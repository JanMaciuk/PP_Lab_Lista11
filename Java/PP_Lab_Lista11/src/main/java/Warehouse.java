import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Receive;

import java.util.ArrayList;

public class Warehouse extends ProductionStage {

    /** How many units of each resource are shipped per time tick*/
    static int shippedAmount = 1;

    /** If set, will print all messages sent between actors. */
    static boolean debugMessagesPrinted = true;

    /** Production stages that the warehouse sends shipments to*/
    ArrayList<ActorRef<Resource>> shipmentReceivers;

    /** Resources that can be shipped to recipients*/
    ArrayList<Resource> toBeShipped;

    /** Finished goods received from production*/
    ArrayList<Resource> received;

    public Warehouse(ArrayList<Resource> toBeShipped) {
        super(ProductionType.Warehouse, 1, null);
        this.shipmentReceivers = new ArrayList<>();
        this.toBeShipped = toBeShipped;
        this.received = new ArrayList<>();
    }

    public void setShipmentReceivers(ArrayList<ActorRef<Resource>> shipmentReceivers) {
        this.shipmentReceivers = shipmentReceivers;
    }

    @Override
    public Receive<Resource> createReceive() {
        return newReceiveBuilder().onMessage(Resource.class, this::send).build();
    }

    private Behavior<Resource> send(Resource receivedResource) {

        if (receivedResource.type == ResourceType.TimeTick) {               // If it's a time tick,
            for (int i = 0; i < shipmentReceivers.size(); i++) {            // send resources to receivers.
                shipmentReceivers.get(i).tell(new Resource(toBeShipped.get(i).type, shippedAmount));    // Resources are index matched with receivers
            }
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
}
