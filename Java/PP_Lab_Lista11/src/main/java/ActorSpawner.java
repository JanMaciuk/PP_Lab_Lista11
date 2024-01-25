import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ActorContext;

import java.util.ArrayList;

public class ActorSpawner extends AbstractBehavior<Resource> {
    static long timeUnitMilis = 100;
    static int actorsMaxIdleWait = 15;
    static int allActorsIdle = actorsMaxIdleWait;
    private static final ArrayList<ActorRef<Resource>> actors = new ArrayList<>();
    public static Behavior<Resource> create() {
        return Behaviors.setup(ActorSpawner::new);
    }
    private ActorSpawner(ActorContext<Resource> context) {
        super(context);
        ArrayList<Resource> startingResources = new ArrayList<>();
        //Set starting resources:
        startingResources.add(new Resource(ResourceType.Fertilizer, 2));
        startingResources.add(new Resource(ResourceType.Meat_raw, 100));
        ActorRef<Resource> MainWarehouse = context.spawn(Warehouse.create(startingResources), "MainWarehouse");
        // Create production stages:
        ActorRef<Resource> canningMachine = context.spawn(ProductionStage.create(ProductionType.CanningMachine, MainWarehouse), "CanningMachine");
        ActorRef<Resource> meatGrill = context.spawn(ProductionStage.create(ProductionType.MeatGrill, canningMachine), "MeatGrill");
        ActorRef<Resource> potatoCutter = context.spawn(ProductionStage.create(ProductionType.PotatoCutter, canningMachine), "PotatoCutter");
        ActorRef<Resource> potatoPeeler = context.spawn(ProductionStage.create(ProductionType.PotatoPeeler, potatoCutter), "PotatoPeeler");
        ActorRef<Resource> farm = context.spawn(ProductionStage.create(ProductionType.Farm, potatoPeeler), "Farm");
        // Set shipment receivers for the warehouse:
        ArrayList<ActorRef<Resource>> warehouseReceivers = new ArrayList<>();
        warehouseReceivers.add(farm);
        warehouseReceivers.add(meatGrill);
        MainWarehouse.tell(new Resource(warehouseReceivers));
        //List all actors:
        actors.add(MainWarehouse);
        actors.add(canningMachine);
        actors.add(meatGrill);
        actors.add(potatoCutter);
        actors.add(potatoPeeler);
        actors.add(farm);
        getContext().getSelf().tell(new Resource(getContext().getSelf()));  // send first time tick to myself, start the system

    }

    @Override
    public Receive<Resource> createReceive() {
        return newReceiveBuilder().onMessage(Resource.class, this::timeTicked).build();
    }

    private Behavior<Resource> timeTicked(Resource message) {

        if (message.receivers.get(0).equals(getContext().getSelf())) { //I send this message to myself
            allActorsIdle--;    // decrement the idle counter
            //Send a time tick to every actor:
            actors.forEach(actor -> actor.tell(new Resource(getContext().getSelf())));
            try {
                Thread.sleep(timeUnitMilis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (allActorsIdle < 0 ) {
                actors.forEach(actor -> actor.tell(new Resource(ResourceType.PrintResourcesCommand, 0)));
                try {
                    Thread.sleep(timeUnitMilis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
            getContext().getSelf().tell(new Resource(getContext().getSelf()));
            }
        else if (allActorsIdle < actorsMaxIdleWait){
            allActorsIdle++;  // another actor sent this message to me, so it's not idle.
        }
        return this;
    }
}
