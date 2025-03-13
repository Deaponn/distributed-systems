import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        System.out.println("JAVA TCP CLIENT");
        String hostName = "localhost";
        InetAddress address = InetAddress.getByName(hostName);
        int portNumber = 12345;
        Socket tcpSocket = null;
        DatagramSocket udpSocket = null;
        Scanner keyboard = new Scanner(System.in);

        System.out.println("Please provide a nickname");
        String nickname = keyboard.nextLine();

        List<String> art = Files.readAllLines(Paths.get("./image.txt"));

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream byteOutput = new DataOutputStream(byteStream);
        for (String element : art) {
            byteOutput.writeUTF(element);
        }
        byte[] buffer = byteStream.toByteArray();

        try {
            tcpSocket = new Socket(hostName, portNumber);

            // in & out streams
            PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

            out.println(nickname);
            int udpPort = Integer.parseInt(in.readLine());
            udpSocket = new DatagramSocket(udpPort);

            Socket finalTcpSocket = tcpSocket;
            DatagramSocket finalUdpSocket = udpSocket;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    finalTcpSocket.close();
                    finalUdpSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

            Thread inputHandler = new Thread(() -> {
                while (true) {
                    String message = keyboard.nextLine();
                    if (message.equals("U")) {
                        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, address, portNumber);
                        try {
                            finalUdpSocket.send(sendPacket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        out.println(message);
                    }
                }
            });

            Thread udpReceiver = new Thread(() -> {
                while (true) {
                    byte[] receiveBuffer = new byte[65507];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    try {
                        finalUdpSocket.receive(receivePacket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println("Received " + receivePacket.getData().length + " bytes of raw data");

                    ByteArrayInputStream byteInputStream = new ByteArrayInputStream(receivePacket.getData());
                    DataInputStream byteInput = new DataInputStream(byteInputStream);
                    while (true) {
                        try {
                            if (!(byteInput.available() > 0)) break;
                            String element = byteInput.readUTF();
                            if (element.isEmpty()) break;
                            System.out.println(element);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });

            inputHandler.start();
            udpReceiver.start();

            while (true) {
                System.out.println(in.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (tcpSocket != null){
                tcpSocket.close();
            }
            if (udpSocket != null){
                udpSocket.close();
            }
        }
    }
}
