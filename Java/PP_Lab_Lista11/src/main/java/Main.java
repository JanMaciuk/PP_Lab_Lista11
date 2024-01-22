
import akka.actor.typed.ActorSystem;

public class Main {
    public static void main(String[] args)  {
        System.out.println("Hello world!");
        final ActorSystem<Resource> spawner = ActorSystem.create(ActorSpawner.create(), "spawnerSystem");
        //TODO: implement success probability in all production stages
        //TODO: automatically end the simulation when production is done
        //TODO: at the end of the simulation, print the amount of resources produced
    }
}