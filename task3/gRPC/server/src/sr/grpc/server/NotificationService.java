package sr.grpc.server;

import io.grpc.stub.StreamObserver;
import sr.grpc.gen.EventInfo;
import sr.grpc.gen.EventNotificationsGrpc.EventNotificationsImplBase;
import sr.grpc.gen.Sport;
import sr.grpc.gen.SubscriptionDetails;

import java.util.HashMap;

public class NotificationService extends EventNotificationsImplBase {
    private final HashMap<String, Client> clients = new HashMap<>();

    public void sendEventInfo(EventInfo eventInfo) {
        for (Client client : this.clients.values()) {
            client.sendIfInterested(eventInfo);
        }
    }

    public void killClient(String id) {
        clients.remove(id);
    }

    public void registerClient(String id, Client newClient) {
        if (clients.containsKey(id)) {
            Client oldClient = clients.get(id);
            newClient.setSubscriptions(oldClient.getSubscriptions());
            for (EventInfo event : oldClient.getDisconnectedBuffer()) {
                newClient.sendIfInterested(event);
            }
        }
        clients.put(id, newClient);
    }

    @Override
    public StreamObserver<SubscriptionDetails> subscribeTo(final StreamObserver<EventInfo> responseObserver) {
        System.out.println("new client");
        return new Client(responseObserver, this);
    }
}
