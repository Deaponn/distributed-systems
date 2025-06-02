import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Team {
    private static final String EXCHANGE_NAME = "orders_exchange";

    public static void main(String[] args) throws IOException, TimeoutException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter team name: ");
        String teamName = scanner.nextLine();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        String confirmationQueueName = teamName.toLowerCase();
        channel.queueDeclare(confirmationQueueName, false, false, false, null);
        channel.queueBind(confirmationQueueName, EXCHANGE_NAME, confirmationQueueName);

        System.out.println("Team '" + teamName + "' started");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[CONFIRMATION RECEIVED] " + message);
        };
        channel.basicConsume(confirmationQueueName, true, deliverCallback, consumerTag -> {});

        System.out.print("Enter equipment to order (or 'exit' to close):\n");
        while (true) {
            String order = scanner.nextLine();

            if (order.isBlank()) {
                break;
            }

            String routingKey = order.toLowerCase();
            String message = confirmationQueueName.toLowerCase();

            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("[ORDER SENT] Order for '" + order + "' sent.");
        }

        System.out.println("Closing team application.");
        channel.close();
        connection.close();
        scanner.close();
    }
}
