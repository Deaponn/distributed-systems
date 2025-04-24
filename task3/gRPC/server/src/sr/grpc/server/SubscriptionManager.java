package sr.grpc.server;

import sr.grpc.gen.EventInfo;
import sr.grpc.gen.Sport;
import sr.grpc.gen.SubscriptionDetails;

import java.util.LinkedList;
import java.util.Objects;

public class SubscriptionManager {
    private final LinkedList<Pair<String, Sport>> subscribedTo = new LinkedList<>();

    public void addSubscription(String place, Sport sport) {
        Pair<String, Sport> pair = new Pair<>(place, sport);
        if (!subscribedTo.contains(pair))
            subscribedTo.add(new Pair<>(place, sport));
    }

    public boolean isInterested(String place, Sport sport) {
        Pair<String, Sport> pair = new Pair<>(place, sport);

        for (Pair<String, Sport> interest : this.subscribedTo) {
            if (interest.equals(pair)) return true;
        }

        return false;
    }

    public EventInfo appendSubscriptions(String id, EventInfo eventInfo) {
        EventInfo.Builder builder = eventInfo.toBuilder();
        for (Pair<String, Sport> sub : subscribedTo)
            builder.addSubscriptions(
                    SubscriptionDetails
                            .newBuilder()
                            .setSubscriberId(id)
                            .setPlace(sub.left())
                            .setSport(sub.right())
                            .build()
            );

        return builder.build();
    }
}

record Pair<K, V>(K left, V right) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
