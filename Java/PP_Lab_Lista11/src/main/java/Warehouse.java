import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Behaviors;

import java.util.ArrayList;

public class Warehouse extends ProductionStage {

    /** How many units of each resource are shipped per time tick*/
    static int shippedAmount = 1;

    /** If set, will print all messages sent between actors. */
    static boolean debugMessagesPrinted = false;

    /** Production stages that the warehouse sends shipments to*/
    ArrayList<ActorRef<Resource>> shipmentReceivers;

    /** Resources that can be shipped to recipients*/
    ArrayList<Resource> toBeShipped;

    /** Finished goods received from production*/
    ArrayList<Resource> received;

    public static Behavior<Resource> create(ArrayList<Resource> toBeShipped) {
        return Behaviors.setup(context -> new Warehouse(toBeShipped, context));
    }

    private Warehouse(ArrayList<Resource> toBeShipped, ActorContext<Resource> context) {
        super(ProductionType.Warehouse, context, null);
        this.shipmentReceivers = new ArrayList<>();
        this.toBeShipped = toBeShipped;
        this.received = new ArrayList<>();
    }

    @Override
    public Receive<Resource> createReceive() {
        return newReceiveBuilder().onMessage(Resource.class, this::send).build();
    }

    private Behavior<Resource> send(Resource receivedResource) {
        if (receivedResource.type == ResourceType.WarehouseReceiverList) {
            this.shipmentReceivers = receivedResource.receivers;
        } else if (receivedResource.type == ResourceType.TimeTick) {    // If it's a time tick,
            for (int i = 0; i < shipmentReceivers.size(); i++) {        // send resources to receivers.
                long amountToShip = Math.max(0, Math.min(toBeShipped.get(i).amount, shippedAmount)); // Check how much to send
                if (amountToShip == 0) { continue; }                    // Don't ship negative amounts
                if (debugMessagesPrinted) {
                    System.out.println("Warehouse sent " + shippedAmount + " " + toBeShipped.get(i).type + " to " + shipmentReceivers.get(i).path().name()+ ", " + toBeShipped.get(i).amount + " were in warehouse");
                }
                shipmentReceivers.get(i).tell(new Resource(toBeShipped.get(i).type, shippedAmount));    // Resources are index matched with receivers
                toBeShipped.get(i).amount -= shippedAmount;                                             // Remove shipped resources from the warehouse
            }
        } else if (receivedResource.type == ResourceType.PrintResourcesCommand) {
            printResources();
        } else { // add resources to the received list, if they already exist increase the amount
            boolean found = false;
            for (Resource r : received) {
                if (r.type == receivedResource.type) {
                    r.amount += receivedResource.amount;
                    found = true;
                    break;
                }
            }
            if (!found) {
                received.add(receivedResource);
            }
        }
        return this;
    }
    private void printResources() {
        //Build string to print:
        StringBuilder sb = new StringBuilder();
        sb.append("Warehouse resources: ");
        for (Resource r : received) {
            sb.append(r.amount);
            sb.append(" ");
            sb.append(r.type);
            sb.append(", ");
        }
        for (Resource r : toBeShipped) {
            sb.append(r.amount);
            sb.append(" ");
            sb.append(r.type);
            sb.append(", ");
        }
        System.out.println(sb);
    }
}
