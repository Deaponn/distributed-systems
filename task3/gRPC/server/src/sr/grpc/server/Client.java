package sr.grpc.server;

import io.grpc.stub.StreamObserver;
import sr.grpc.gen.EventInfo;
import sr.grpc.gen.SubscriptionDetails;

import java.util.LinkedList;

public class Client implements StreamObserver<SubscriptionDetails> {
    private final StreamObserver<EventInfo> responseObserver;
    private SubscriptionManager subscriptions = new SubscriptionManager();
    private final NotificationService service;
    private final LinkedList<EventInfo> disconnectedBuffer = new LinkedList<>();
    private boolean isDisconnected = false;
    private String id = null;

    public Client(StreamObserver<EventInfo> responseObserver, NotificationService service) {
        this.responseObserver = responseObserver;
        this.service = service;
    }

    public void sendIfInterested(EventInfo eventInfo) {
        if (subscriptions.isInterested(eventInfo.getPlace(), eventInfo.getSport())) {
            EventInfo saturatedEvent = subscriptions.appendSubscriptions(id, eventInfo);
            if (isDisconnected) disconnectedBuffer.add(saturatedEvent);
            else responseObserver.onNext(saturatedEvent);
        }
    }

    public LinkedList<EventInfo> getDisconnectedBuffer() {
        return disconnectedBuffer;
    }

    public SubscriptionManager getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(SubscriptionManager subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public void onNext(SubscriptionDetails newSubscription) {
        if (id == null) { // initial connection
            id = newSubscription.getSubscriberId();
            service.registerClient(id, this);
        } else subscriptions.addSubscription(newSubscription.getPlace(), newSubscription.getSport());
    }

    @Override
    public void onError(Throwable t) { // now should start buffering for this client
        isDisconnected = true;
    }

    @Override
    public void onCompleted() {
        service.killClient(id);
        System.out.println("graceful exit");
        responseObserver.onCompleted();
    }
}
