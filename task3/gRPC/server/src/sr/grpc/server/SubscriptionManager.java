package sr.grpc.server;

import sr.grpc.gen.Sport;

import java.util.LinkedList;
import java.util.Objects;

public class SubscriptionManager {
    private final LinkedList<Pair<String, Sport>> subscribedTo = new LinkedList<>();

    public void addSubscription(String place, Sport sport) {
        subscribedTo.add(new Pair<>(place, sport));
    }

    public boolean isInterested(String place, Sport sport) {
        Pair<String, Sport> pair = new Pair<>(place, sport);

        for (Pair<String, Sport> interest : this.subscribedTo) {
            if (interest.equals(pair)) return true;
        }

        return false;
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
