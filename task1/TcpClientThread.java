import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

public class TcpClientThread implements Runnable {
    private final String nickname;
    private final BufferedReader input;
    private final ConnectionManager connections;

    public TcpClientThread(String nickname, BufferedReader input, ConnectionManager connections) {
        this.nickname = nickname;
        this.input = input;
        this.connections = connections;
    }

    @Override
    public void run() {
        System.out.println("Opening Tcp connection with " + nickname);

        try {
            while (true) {
                String msg = this.input.readLine();
                if (msg == null) break;
                System.out.println("Received Tcp message " + msg + " from " + this.nickname);
                this.connections.forEveryConnection((nick, writer) -> {
                    if (Objects.equals(nick, this.nickname)) return;
                    System.out.println("    Sending the above message to " + nick);
                    writer.println(nickname + ": " + msg);
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Closing Tcp connection with " + nickname);
            this.connections.removeConnection(this.nickname);
        }
    }
}
