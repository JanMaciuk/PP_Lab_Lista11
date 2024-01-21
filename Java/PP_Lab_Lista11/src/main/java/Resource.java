public class Resource {
    long amount;
    ResourceType type;

    public Resource(ResourceType type, long amount) {
        this.type = type;
        this.amount = amount;
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
}