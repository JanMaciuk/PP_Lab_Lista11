import java.util.ArrayList;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ActorContext;

public class ProductionStage extends AbstractBehavior<Resource> {
    /** Static time modifier, used to change the speed of the simulation. */
    static int timeModifier = 1;

    /** If set, will print all messages sent between actors. */
    static boolean debugMessagesPrinted = true;

    /** The resources that the production stage will process, amount is the needed quantity. */
    ArrayList<Resource> inputs;

    /** The resources that are about to be processed, amount is the current quantity. */
    ArrayList<Resource> processing;

    /** The resources that the production stage will produce, amount is the produced quantity. */
    ArrayList<Resource> outputs;

    /** The probability that the production stage will give an output. */
    double successRate;

    /** How many time units it takes to process the resources. */
    int processingTime;

    /** The type of production stage. */
    ProductionType type;

    /** Jobs that are currently undergoing, represented as the time left until completion */
    ArrayList<Integer> ongoingJobs;

    /** Maximum amount of jobs that can be underway at the same time */
    int maxJobs;

    /** Production stages that this stage sends finished goods to */
    ActorRef<Resource> nextStage;

    public static Behavior<Resource> create(ProductionType type, ActorRef<Resource> nextStage) {
        return Behaviors.setup(context -> new ProductionStage(type, context, nextStage));
    }

    public ProductionStage(ProductionType type,ActorContext<Resource> context,ActorRef<Resource> nextStage) {
        super(context);
        this.type = type;
        this.inputs = new ArrayList<>();
        this.processing = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.ongoingJobs = new ArrayList<>();
        this.nextStage = nextStage;
        switch (type) {
            case Warehouse -> {
                this.inputs.add(new Resource(ResourceType.Canned_food, 1));
                this.processing.add(new Resource(ResourceType.Canned_food, 0));
                this.outputs.add(new Resource(ResourceType.Fertilizer, 1));
                this.outputs.add(new Resource(ResourceType.Meat_raw, 1));
                this.processingTime = timeModifier;
                this.successRate = 1;
                this.maxJobs = 1;
            }
            case Farm -> {
                this.inputs.add(new Resource(ResourceType.Fertilizer, 1));
                this.processing.add(new Resource(ResourceType.Fertilizer, 0));
                this.outputs.add(new Resource(ResourceType.Potato_raw, 50));
                this.processingTime = 10*timeModifier;
                this.successRate = 0.75;
                this.maxJobs = 1;
            }
            case PotatoPeeler -> {
                this.inputs.add(new Resource(ResourceType.Potato_raw, 10));
                this.processing.add(new Resource(ResourceType.Potato_raw, 0));
                this.outputs.add(new Resource(ResourceType.Potato_peeled, 10));
                this.processingTime = 2*timeModifier;
                this.successRate = 0.95;
                this.maxJobs = 2;
            }
            case PotatoCutter -> {
                this.inputs.add(new Resource(ResourceType.Potato_peeled, 10));
                this.processing.add(new Resource(ResourceType.Potato_peeled, 0));
                this.outputs.add(new Resource(ResourceType.Potato_cut, 10));
                this.processingTime = 2*timeModifier;
                this.successRate = 0.95;
                this.maxJobs = 2;
            }
            case MeatGrill -> {
                this.inputs.add(new Resource(ResourceType.Meat_raw, 5));
                this.processing.add(new Resource(ResourceType.Meat_raw, 0));
                this.outputs.add(new Resource(ResourceType.Meat_fried, 4));
                this.processingTime = 4*timeModifier;
                this.successRate = 0.8;
                this.maxJobs = 1;
            }
            case CanningMachine -> {
                this.inputs.add(new Resource(ResourceType.Potato_cut, 5));
                this.inputs.add(new Resource(ResourceType.Meat_fried, 4));
                this.processing.add(new Resource(ResourceType.Potato_cut, 0));
                this.processing.add(new Resource(ResourceType.Meat_fried, 0));
                this.outputs.add(new Resource(ResourceType.Canned_food, 1));
                this.processingTime = 5*timeModifier;
                this.successRate = 0.95;
                this.maxJobs = 1;
            }
        }
    }

    @Override
    public Receive<Resource> createReceive() {
        return newReceiveBuilder().onMessage(Resource.class, this::processResource).build();
    }

    private Behavior<Resource> processResource(Resource receivedResource) {
        if (receivedResource.type == ResourceType.TimeTick) {
            doTimeTick(receivedResource.receivers.get(0));
            return Behaviors.same();
        }
        if (receivedResource.type == ResourceType.PrintResourcesCommand) {
            printResources();
            return Behaviors.same();
        }

        boolean validInput = false;
        int inputIndex = 0;
        for (int i = 0; i < inputs.size(); i++) {   // Check if the received resource is an accepted input
            if (inputs.get(i).type == receivedResource.type) {
                validInput = true;
                inputIndex = i;
                break;
            }
        }

        if (validInput) {  // If the received resource is an accepted input, store it in preprocessing
            processing.get(inputIndex).amount += receivedResource.amount;
        } else {
            throw new IllegalArgumentException("Passed an invalid resource:"+ receivedResource.type + ", to a production stage: " + this.type);
        }
    return Behaviors.same();
    }

    private void doTimeTick(ActorRef<Resource> spawnerRef) {
        //decrease time left for ongoing jobs
        ongoingJobs.replaceAll(timeLeft -> timeLeft - 1);

        //If a job is done, send the output to the next stage:
        ongoingJobs.forEach(integer -> {
            if (integer <= 0) {
                if (debugMessagesPrinted) {
                    System.out.println(this.type + " sent a resource: " + outputs.get(0).type + " to the next stage.");
                }
                if (Math.random() < successRate) { // Only send the output if the production was successful
                    nextStage.tell(outputs.get(0));
               }
            }
        });
        ongoingJobs.removeIf(integer -> integer <= 0);  // Remove all jobs that are done

        if (!ongoingJobs.isEmpty()) {   // if we are busy, respond with a time tick to the controller
            spawnerRef.tell(new Resource(getContext().getSelf()));
        }

        boolean canStartNewJob = true;
        for (int i = 0; i < inputs.size(); i++) {
            if (processing.get(i).amount < inputs.get(i).amount) {  // If there isn't enough of a resource, prevent starting a new job
                canStartNewJob = false;
                break;
            }
        }
        if (canStartNewJob && ongoingJobs.size() < maxJobs) {   // If there is enough of all resources, and there is room for a new job.
            ongoingJobs.add(processingTime);                    // Start a new job,
            for (int i = 0; i < inputs.size(); i++) {           // and remove resources used by the job from preprocessing
                processing.get(i).amount -= inputs.get(i).amount;
            }
            if (debugMessagesPrinted) {
                System.out.println( this.type + " started a new job, " + processing.get(0).amount + " " + processing.get(0).type + " are left.");
            }
        }
    }

    private void printResources() {
       //Build strig to print:
        StringBuilder output = new StringBuilder(this.type + " has: ");
        for (Resource r : processing) {
            output.append(r.amount).append(" ").append(r.type).append(", ");
        }

        System.out.println(output);
    }


}

enum ProductionType {
    Warehouse,
    Farm,
    PotatoPeeler,
    PotatoCutter,
    MeatGrill,
    CanningMachine,

}