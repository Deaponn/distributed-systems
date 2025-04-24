package sr.grpc.server;

import io.grpc.stub.StreamObserver;
import sr.grpc.gen.EventInfo;
import sr.grpc.gen.Sport;
import sr.grpc.gen.SubscriptionDetails;

public class Client implements StreamObserver<SubscriptionDetails> {
    private final StreamObserver<EventInfo> responseObserver;

    private final SubscriptionManager subscriptions = new SubscriptionManager();

    private final NotificationService service;

    public Client(StreamObserver<EventInfo> responseObserver, NotificationService service) {
        this.responseObserver = responseObserver;
        this.service = service;
    }

    public void sendIfInterested(String place, Sport sport, EventInfo eventInfo) {
        if (subscriptions.isInterested(place, sport)) responseObserver.onNext(eventInfo);
    }

    @Override
    public void onNext(SubscriptionDetails newSubscription) {
        subscriptions.addSubscription(newSubscription.getPlace(), newSubscription.getSport());
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Encountered error in routeChat" + t);
    }

    @Override
    public void onCompleted() {
        service.killClient(this);
        responseObserver.onCompleted();
    }
}
