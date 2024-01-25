
import akka.actor.typed.ActorSystem;

public class Main {
    public static void main(String[] args)  {
        System.out.println("Hello world!");
        final ActorSystem<Resource> spawner = ActorSystem.create(ActorSpawner.create(), "spawnerSystem");
    }
}