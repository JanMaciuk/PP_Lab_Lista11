import akka.actor.typed.ActorRef;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        ArrayList<Resource> startingResources = new ArrayList<>();
        //Set starting resources:
        startingResources.add(new Resource(ResourceType.Fertilizer, 100));
        startingResources.add(new Resource(ResourceType.Meat_raw, 100));
        Warehouse MainWarehouse = new Warehouse(startingResources);
        // Create production stages:
        ProductionStage CanningMachine = new ProductionStage(ProductionType.CanningMachine, 1, MainWarehouse.getContext().getSelf());
        ProductionStage MeatGrill =      new ProductionStage(ProductionType.MeatGrill, 1, CanningMachine.getContext().getSelf());
        ProductionStage PotatoCutter =   new ProductionStage(ProductionType.PotatoCutter, 1, CanningMachine.getContext().getSelf());
        ProductionStage PotatoPeeler =   new ProductionStage(ProductionType.PotatoPeeler, 1, PotatoCutter.getContext().getSelf());
        ProductionStage Farm =           new ProductionStage(ProductionType.Farm, 1, PotatoPeeler.getContext().getSelf());
        // Set shipment receivers for the warehouse:
        ArrayList<ActorRef<Resource>> warehouseReceivers = new ArrayList<>();
        warehouseReceivers.add(Farm.getContext().getSelf());
        warehouseReceivers.add(MeatGrill.getContext().getSelf());
        MainWarehouse.setShipmentReceivers(warehouseReceivers);


    }
}