import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

public class JavaUdpServerEndianness {

    public static void main(String args[])
    {
        System.out.println("JAVA UDP SERVER");
        DatagramSocket socket = null;
        int portNumber = 9009;

        try{
            socket = new DatagramSocket(portNumber);
            byte[] receiveBuffer = new byte[4];

            while(true) {
                Arrays.fill(receiveBuffer, (byte)0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                byte[] received = receivePacket.getData();

                int nb = ByteBuffer.wrap(received).getInt();

                System.out.println("received msg: " + nb);
                System.out.println("received msg: " + received);
                byte[] buff = ByteBuffer.allocate(4).putInt(nb + 1).array();

                DatagramPacket responsePacket = new DatagramPacket(buff, buff.length, receivePacket.getAddress(), receivePacket.getPort());
                socket.send(responsePacket);
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
