import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        }
        finally{
            if (tcpSocket != null){
                tcpSocket.close();
            }
            if (udpSocket != null){
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

                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, receivePacket.getAddress(), receivePacket.getPort());
                    connections.forEverySocket((nickname, socket) -> {
                        if (socket.getPort() == receivePacket.getPort()) {
                            System.out.println("Propagating UDP byte data from: " + nickname);
                            return;
                        }
                        try {
                            responsePacket.setAddress(socket.getInetAddress());
                            responsePacket.setPort(socket.getPort());
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
}
