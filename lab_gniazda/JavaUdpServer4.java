import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class JavaUdpServer4 {

    public static void main(String args[])
    {
        System.out.println("JAVA UDP SERVER");
        DatagramSocket socket = null;
        int portNumber = 9008;

        try{
            socket = new DatagramSocket(portNumber);
            byte[] receiveBuffer = new byte[1024];
            byte[] javaResponseBuffer = "Pong Java Udp".getBytes();
            byte[] pythonResponseBuffer = "Pong Python Udp".getBytes();

            while(true) {
                Arrays.fill(receiveBuffer, (byte)0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                String msg = new String(receivePacket.getData());
                System.out.println("received msg: " + msg.trim());

                if (msg.trim().equals("Ping Java Udp")) {
                    DatagramPacket responsePacket = new DatagramPacket(javaResponseBuffer, javaResponseBuffer.length, receivePacket.getAddress(), receivePacket.getPort());
                    socket.send(responsePacket);
                } else {
                    DatagramPacket responsePacket = new DatagramPacket(pythonResponseBuffer, pythonResponseBuffer.length, receivePacket.getAddress(), receivePacket.getPort());
                    socket.send(responsePacket);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
