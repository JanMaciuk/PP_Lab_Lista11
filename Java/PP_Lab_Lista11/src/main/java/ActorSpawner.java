import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ActorContext;

import java.util.ArrayList;

public class ActorSpawner extends AbstractBehavior<Resource> {
    static long simulationLength = 100;    //TODO: automatically end the simulation
    static long timeUnitMilis = 100;
    public static Behavior<Resource> create() {
        return Behaviors.setup(ActorSpawner::new);
    }
    private ActorSpawner(ActorContext<Resource> context) {
        super(context);
        ArrayList<Resource> startingResources = new ArrayList<>();
        //Set starting resources:
        startingResources.add(new Resource(ResourceType.Fertilizer, 5));
        startingResources.add(new Resource(ResourceType.Meat_raw, 20));
        //Warehouse MainWarehouse = new Warehouse(startingResources, context);
        ActorRef<Resource> MainWarehouse = context.spawn(Warehouse.create(startingResources), "MainWarehouse");
        // Create production stages:
        //ProductionStage CanningMachine = new ProductionStage(ProductionType.CanningMachine, context,  MainWarehouse.getContext().getSelf());
        ActorRef<Resource> canningMachine = context.spawn(ProductionStage.create(ProductionType.CanningMachine, MainWarehouse), "CanningMachine");
        //ProductionStage MeatGrill =      new ProductionStage(ProductionType.MeatGrill, context,  CanningMachine.getContext().getSelf());
        ActorRef<Resource> meatGrill = context.spawn(ProductionStage.create(ProductionType.MeatGrill, canningMachine), "MeatGrill");
        //ProductionStage PotatoCutter =   new ProductionStage(ProductionType.PotatoCutter, context,  CanningMachine.getContext().getSelf());
        ActorRef<Resource> potatoCutter = context.spawn(ProductionStage.create(ProductionType.PotatoCutter, canningMachine), "PotatoCutter");
        //ProductionStage PotatoPeeler =   new ProductionStage(ProductionType.PotatoPeeler, context,  PotatoCutter.getContext().getSelf());
        ActorRef<Resource> potatoPeeler = context.spawn(ProductionStage.create(ProductionType.PotatoPeeler, potatoCutter), "PotatoPeeler");
       // ProductionStage Farm =           new ProductionStage(ProductionType.Farm, context,  PotatoPeeler.getContext().getSelf());
        ActorRef<Resource> farm = context.spawn(ProductionStage.create(ProductionType.Farm, potatoPeeler), "Farm");
        // Set shipment receivers for the warehouse:
        ArrayList<ActorRef<Resource>> warehouseReceivers = new ArrayList<>();
        warehouseReceivers.add(farm);
        warehouseReceivers.add(meatGrill);
        MainWarehouse.tell(new Resource(warehouseReceivers));
        //List all actors:
        ArrayList<ActorRef<Resource>> actors = new ArrayList<>();
        actors.add(MainWarehouse);
        actors.add(canningMachine);
        actors.add(meatGrill);
        actors.add(potatoCutter);
        actors.add(potatoPeeler);
        actors.add(farm);


        //Run the simulation:
        while (simulationLength > 0) {
            //Send a time tick to every actor:
            actors.forEach(actor -> actor.tell(new Resource(ResourceType.TimeTick, 1)));
            try {
                Thread.sleep(timeUnitMilis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            simulationLength--;
            System.out.println("Time left: " + simulationLength + " time units");
        }
    }

    @Override
    public Receive<Resource> createReceive() {
        return newReceiveBuilder().onMessage(Resource.class, this::timeTicked).build();
    }

    private Behavior<Resource> timeTicked(Resource message) {
        System.out.println("One unit of time has passed");
        return this;
    }
}
