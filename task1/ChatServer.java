import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    public static void main(String[] args) throws IOException {
        int portNumber = 12345;
        ServerSocket tcpSocket = null;
        DatagramSocket udpSocket = null;
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        ConnectionManager connections = new ConnectionManager();

        try {
            tcpSocket = new ServerSocket(portNumber);
            udpSocket = new DatagramSocket(portNumber);

            Thread udpThread = getUdpThread(udpSocket, connections);
            udpThread.start();

            System.out.println("CHAT SERVER RUNNING");

            while (true) {
                Socket tcpClientSocket = tcpSocket.accept();

                BufferedReader reader = new BufferedReader(new InputStreamReader(tcpClientSocket.getInputStream()));
                String nickname = reader.readLine();

                final String finalNickname = connections.addConnection(nickname, tcpClientSocket);
                TcpClientThread tcpClient = new TcpClientThread(finalNickname, reader, connections);

                threadPool.submit(tcpClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (tcpSocket != null) {
                tcpSocket.close();
            }
            if (udpSocket != null) {
                udpSocket.close();
            }
        }
    }

    private static Thread getUdpThread(DatagramSocket udpSocket, ConnectionManager connections) {
        byte[] buffer = new byte[65507];

        return new Thread(() -> {
            while (true) {
                Arrays.fill(buffer, (byte) 0);
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                try {
                    udpSocket.receive(receivePacket);

                    connections.forEverySocket((nickname, socket) -> {
                        if (socket.getPort() == receivePacket.getPort()) {
                            System.out.println("Received UDP message from: " + nickname);
                            return;
                        }
                        try {
                            byte[] newBuffer = modifyBufferToIncludeNickname(buffer, nickname);
                            DatagramPacket responsePacket = new DatagramPacket(newBuffer, newBuffer.length, socket.getInetAddress(), socket.getPort());
                            udpSocket.send(responsePacket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static byte[] modifyBufferToIncludeNickname(byte[] buffer, String nickname) {
        String padding = "+".repeat((18 - nickname.length()) / 2);
        nickname = String.format("%18s", padding + nickname + padding).replace(" ", "+");

        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(buffer);
        DataInputStream byteInput = new DataInputStream(byteInputStream);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream byteOutput = new DataOutputStream(byteStream);
        while (true) {
            try {
                if (!(byteInput.available() > 0)) break;
                String element = byteInput.readUTF();
                if (element.isEmpty()) break;
                element = element.replace("++++++++++++++++++", nickname);
                byteOutput.writeUTF(element);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return byteStream.toByteArray();
    }
}
