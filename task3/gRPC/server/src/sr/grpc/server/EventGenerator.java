package sr.grpc.server;

import sr.grpc.gen.EventInfo;
import sr.grpc.gen.Player;
import sr.grpc.gen.Sport;

import java.util.List;
import java.util.Random;

import static java.lang.Thread.sleep;

public class EventGenerator {
    private final List<String> names = List.of("Peter", "Harold", "Robert", "Mike", "Julia");
    private final Random randomizer = new Random(42);

    public EventGenerator(NotificationService notifications) {
        List<String> places = List.of("Krakow", "Wroclaw", "Solec-Zdroj");

        while (true) {
            notifications.sendEventInfo(
                    this.generateEvent(this.randomFromList(places), this.selectSport())
            );

            try {
                sleep(1000);
            } catch (InterruptedException ignored) {}
        }
    }

    private <E> E randomFromList(List<E> list) {
        return list.get(randomizer.nextInt(list.size()));
    }

    private Sport selectSport() {
        return Sport.forNumber(randomizer.nextInt(5));
    }

    private Player randomPlayer() {
        return Player.newBuilder()
                        .setName(randomFromList(names))
                        .setNumber(1 + randomizer.nextInt(99))
                        .build();
    }

    private EventInfo generateEvent(String place, Sport sport) {
        System.out.println("generating " + place + " " + sport);

        EventInfo.Builder event = EventInfo.newBuilder()
                .setPlace(place)
                .setSport(sport)
                .setPrize(100 + 10 * randomizer.nextInt(100))
                .addPlayers(randomPlayer())
                .addPlayers(randomPlayer());

        while (randomizer.nextInt(10) < 7) {
                event.addPlayers(randomPlayer());
        }

        return event.build();
    }
}
