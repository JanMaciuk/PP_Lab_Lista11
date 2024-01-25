import akka.actor.typed.ActorRef;

import java.util.ArrayList;

public class Resource {
    long amount;
    ResourceType type;
    ArrayList<ActorRef<Resource>> receivers;

    public Resource(ResourceType type, long amount) {
        this.type = type;
        this.amount = amount;
        receivers = null;
    }
    public Resource(ActorRef<Resource> selfReference) {
        this.type = ResourceType.TimeTick;
        this.amount = 0;
        this.receivers = new ArrayList<>();
        this.receivers.add(selfReference);
    }

    public Resource(ArrayList<ActorRef<Resource>> receivers) {
        this.type = ResourceType.WarehouseReceiverList;
        this.amount = 0;
        this.receivers = receivers;
    }
}

enum ResourceType {
    Fertilizer,
    Potato_raw,
    Potato_peeled,
    Potato_cut,
    Meat_raw,
    Meat_fried,
    Canned_food,
    TimeTick,
    WarehouseReceiverList,
    PrintResourcesCommand
}