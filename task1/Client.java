import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        System.out.println("JAVA TCP CLIENT");

        String hostName = "localhost";
        InetAddress address = InetAddress.getByName(hostName);
        InetAddress multicastGroup = InetAddress.getByName("224.0.0.1");

        int portNumber = 12345;

        Socket tcpSocket = null;
        DatagramSocket udpSocket = null;
        MulticastSocket multicastSocket = null;

        Scanner keyboard = new Scanner(System.in);

        System.out.println("Please provide a nickname");
        String nickname = keyboard.nextLine();
        AsciiArts arts = getAsciiArts(nickname);

        try {
            tcpSocket = new Socket(hostName, portNumber);
            multicastSocket = new MulticastSocket(portNumber + 1);
            multicastSocket.joinGroup(new InetSocketAddress(multicastGroup, portNumber), null);

            PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

            out.println(nickname);
            int udpPort = Integer.parseInt(in.readLine());
            udpSocket = new DatagramSocket(udpPort);

            Socket finalTcpSocket = tcpSocket;
            DatagramSocket finalUdpSocket = udpSocket;
            MulticastSocket finalMulticastSocket = multicastSocket;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    finalTcpSocket.close();
                    finalUdpSocket.close();
                    finalMulticastSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

            Thread inputHandler = new Thread(() -> {
                while (true) {
                    String message = keyboard.nextLine();
                    if (message.equals("U")) {
                        DatagramPacket sendPacket = new DatagramPacket(arts.serverArt(), arts.serverArt().length, address, portNumber);
                        try {
                            finalUdpSocket.send(sendPacket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (message.equals("M")) {
                        DatagramPacket sendPacket = new DatagramPacket(arts.multicastArt(), arts.multicastArt().length, multicastGroup, portNumber + 1);
                        try {
                            finalMulticastSocket.send(sendPacket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        out.println(message);
                    }
                }
            });

            Thread udpReceiver = new Thread(getUdpReceiver(finalUdpSocket));

            Thread multicastReceiver = new Thread(getUdpReceiver(finalMulticastSocket));

            inputHandler.start();
            udpReceiver.start();
            multicastReceiver.start();

            while (true) {
                System.out.println(in.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (tcpSocket != null) {
                tcpSocket.close();
            }
            if (udpSocket != null) {
                udpSocket.close();
            }
            if (multicastSocket != null) {
                multicastSocket.close();
            }
        }
    }

    private static AsciiArts getAsciiArts(String nickname) throws IOException {
        String padding = "=".repeat((18 - nickname.length()) / 2);
        String paddedNickname = String.format("%18s", padding + nickname + padding).replace(" ", "=");

        List<String> art = Files.readAllLines(Paths.get("./image.txt"));

        ByteArrayOutputStream serverByteStream = new ByteArrayOutputStream();
        DataOutputStream serverByteOutput = new DataOutputStream(serverByteStream);
        ByteArrayOutputStream multicastByteStream = new ByteArrayOutputStream();
        DataOutputStream multicastByteOutput = new DataOutputStream(multicastByteStream);
        for (String element : art) {
            element = element.replace("==================", paddedNickname);
            serverByteOutput.writeUTF(element);
            multicastByteOutput.writeUTF(element.replace("++++++++++++++++++", "+++++multicast++++"));
        }
        byte[] serverArt = serverByteStream.toByteArray();
        byte[] multicastArt = multicastByteStream.toByteArray();
        AsciiArts arts = new AsciiArts(serverArt, multicastArt);
        return arts;
    }

    private record AsciiArts(byte[] serverArt, byte[] multicastArt) {
    }

    private static Runnable getUdpReceiver(DatagramSocket finalMulticastSocket) {
        return () -> {
            while (true) {
                byte[] receiveBuffer = new byte[65507];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    finalMulticastSocket.receive(receivePacket);
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
        };
    }
}
