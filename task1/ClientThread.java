import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClientThread implements Runnable {
    private final String nickname;
    private final BufferedReader input;
    private final ConnectionManager connections;

    public ClientThread(String nickname, BufferedReader input, ConnectionManager connections) {
        this.nickname = nickname;
        this.input = input;
        this.connections = connections;
    }

    @Override
    public void run() {
        System.out.println("Opening the connection with " + nickname);

        try {
            while (true) {
                String msg = this.input.readLine();
                if (msg == null) break;
                System.out.println("Received message " + msg + " from " + this.nickname);
                this.connections.forEveryConnection((nick, writer) -> {
                    if (Objects.equals(nick, this.nickname)) return;
                    System.out.println("    Sending the above message to " + nick);
                    writer.println(nickname + ": " + msg);
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Closing the connection with " + nickname);
            this.connections.removeConnection(this.nickname);
        }
    }
}
