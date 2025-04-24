package sr.grpc.server;

import sr.grpc.gen.EventInfo;
import sr.grpc.gen.EventNotificationsGrpc.EventNotificationsImplBase;
import sr.grpc.gen.Player;
import sr.grpc.gen.Sport;

import static java.lang.Thread.sleep;

public class NotificationService extends EventNotificationsImplBase {
    @Override
    public void subscribeTo(sr.grpc.gen.SubscriptionDetails request,
                            io.grpc.stub.StreamObserver<sr.grpc.gen.EventInfo> responseObserver) {
        System.out.println("place " + request.getPlace() + ", sport " + request.getSport());

        for (int i = 0; i < 5; i++) {
            EventInfo result = EventInfo.newBuilder()
                    .setPlace("street")
                    .addPlayers(Player.newBuilder().setName("tomek").setNumber(3).build())
                    .addPlayers(Player.newBuilder().setName("tomek2").setNumber(4).build())
                    .addPlayers(Player.newBuilder().setName("tomek3").setNumber(5).build())
                    .setSport(Sport.BASKETBALL)
                    .setPrize(300)
                    .build();

            try {
                sleep(500);
            } catch (InterruptedException ignored) {}

            responseObserver.onNext(result);
        }

        responseObserver.onCompleted();
    }
}
