import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Supplier {
    private static int orderCount = 1;
    private static final String EXCHANGE_NAME = "orders_exchange";

    public static void main(String[] args) throws IOException, TimeoutException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter supplier name: ");
        String supplierName = scanner.nextLine();

        List<String> handledEquipment = new ArrayList<>();
        while (true) {
            System.out.print("Enter a supplied equipment type (or press Enter to finish): ");
            String equipment = scanner.nextLine();
            if (equipment.isBlank()) {
                break;
            }
            handledEquipment.add(equipment.toLowerCase());
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        System.out.println("Supplier '" + supplierName + "' started. Waiting for orders for: " + String.join(", ", handledEquipment));

        DeliverCallback deliverCallback = getDeliverCallback(supplierName, channel);

        for (String equipment : handledEquipment) {
            channel.queueDeclare(equipment, false, false, false, null);
            channel.queueBind(equipment, EXCHANGE_NAME, equipment);
            channel.basicConsume(equipment, true, deliverCallback, consumerTag -> {});
        }
    }

    private static DeliverCallback getDeliverCallback(String supplierName, Channel channel) {
        return (consumerTag, delivery) -> {
            String replyToQueue = new String(delivery.getBody(), StandardCharsets.UTF_8);
            String equipmentType = delivery.getEnvelope().getRoutingKey();
            System.out.println("[ORDER RECEIVED] " + replyToQueue + " ordered " + equipmentType);

            int orderId = orderCount++;
            String confirmationMessage = "Order #" + orderId + " (" + equipmentType + ") confirmed by " + supplierName;

            channel.basicPublish(EXCHANGE_NAME, replyToQueue, null, confirmationMessage.getBytes(StandardCharsets.UTF_8));
            System.out.println("[CONFIRMATION SENT] To " + replyToQueue + " for order #" + orderId);
        };
    }
}
