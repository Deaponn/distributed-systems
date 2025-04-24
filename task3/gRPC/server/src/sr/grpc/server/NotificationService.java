package sr.grpc.server;

import io.grpc.stub.StreamObserver;
import sr.grpc.gen.EventInfo;
import sr.grpc.gen.EventNotificationsGrpc.EventNotificationsImplBase;
import sr.grpc.gen.Sport;
import sr.grpc.gen.SubscriptionDetails;

import java.util.LinkedList;

public class NotificationService extends EventNotificationsImplBase {
    private final LinkedList<Client> clients = new LinkedList<>();

    public void sendEventInfo(String place, Sport sport, EventInfo eventInfo) {
        for (Client client : this.clients) {
            client.sendIfInterested(place, sport, eventInfo);
        }
    }

    public void killClient(Client client) {
        this.clients.remove(client);
    }

    @Override
    public StreamObserver<SubscriptionDetails> subscribeTo(final StreamObserver<EventInfo> responseObserver) {
        Client client = new Client(responseObserver, this);
        this.clients.add(client);
        return client;
    }
}
