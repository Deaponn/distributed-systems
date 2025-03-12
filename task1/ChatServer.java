import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    public static void main(String[] args) throws IOException {
        int portNumber = 12345;
        ServerSocket serverSocket = null;
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        ConnectionManager connections = new ConnectionManager();

        try {
            // create socket
            serverSocket = new ServerSocket(portNumber);
            System.out.println("CHAT SERVER RUNNING");

            while (true) {
                Socket socket = serverSocket.accept();

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String nickname = reader.readLine();

                final String finalNickname = connections.addConnection(nickname, socket);
                ClientThread client = new ClientThread(finalNickname, reader, connections);

                threadPool.submit(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            if (serverSocket != null){
                serverSocket.close();
            }
        }
    }

}
