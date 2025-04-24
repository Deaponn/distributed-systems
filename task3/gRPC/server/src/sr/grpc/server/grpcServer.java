package sr.grpc.server;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import sr.grpc.gen.EventInfo;
import sr.grpc.gen.Player;
import sr.grpc.gen.Sport;

import static java.lang.Thread.sleep;


public class grpcServer 
{
	private static final Logger logger = Logger.getLogger(grpcServer.class.getName());

	private Server server;


	private void start() throws IOException
	{
		String address = "127.0.0.1";
		int port = 50051;

		NotificationService notificationService = new NotificationService();

		//You will want to employ flow-control so that the queue doesn't blow up your memory. You can cast StreamObserver to CallStreamObserver to get flow-control API
		server = ServerBuilder.forPort(50051).executor((Executors.newFixedThreadPool(16)))
				//NettyServerBuilder.forAddress(socket).executor(Executors.newFixedThreadPool(16))
				.addService(notificationService)
				.build()
				.start();
		logger.info("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Use stderr here since the logger may have been reset by its JVM shutdown hook.
				System.err.println("Shutting down gRPC server...");
				grpcServer.this.stop();
				System.err.println("Server shut down.");
			}
		});

		while (true) {
			EventInfo result = EventInfo.newBuilder()
					.setPlace("street")
					.addPlayers(Player.newBuilder().setName("tomek").setNumber(3).build())
					.addPlayers(Player.newBuilder().setName("tomek2").setNumber(4).build())
					.addPlayers(Player.newBuilder().setName("tomek3").setNumber(5).build())
					.setSport(Sport.BASKETBALL)
					.setPrize(300)
					.build();
			notificationService.sendEventInfo("krakow", Sport.FOOTBALL, result);

			try {
				sleep(1000);
			} catch (InterruptedException ignored) {}
		}
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	/**
	 * Main launches the server from the command line.
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		final grpcServer server = new grpcServer();
		server.start();
		server.blockUntilShutdown();
	}

}
