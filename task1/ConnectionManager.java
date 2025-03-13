import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

public class ConnectionManager {
    private final HashMap<String, Socket> tcpSockets = new HashMap<>();
    private final HashMap<String, PrintWriter> tcpWriters = new HashMap<>();
    private final ReentrantLock tcpLock = new ReentrantLock();

    public void forEveryTcpConnection(BiConsumer<String, PrintWriter> callback) {
        try {
            this.tcpLock.lock();
            for (Map.Entry<String, PrintWriter> entry : this.tcpWriters.entrySet()) {
                callback.accept(entry.getKey(), entry.getValue());
            }
        } finally {
            this.tcpLock.unlock();
        }
    }

    public void forEverySocket(BiConsumer<String, Socket> callback) {
        try {
            this.tcpLock.lock();
            for (Map.Entry<String, Socket> entry : this.tcpSockets.entrySet()) {
                callback.accept(entry.getKey(), entry.getValue());
            }
        } finally {
            this.tcpLock.unlock();
        }
    }

    public String addConnection(String nickname, Socket tcpSocket) throws IOException {
        try {
            this.tcpLock.lock();
            PrintWriter writer = new PrintWriter(tcpSocket.getOutputStream(), true);
            writer.println(tcpSocket.getPort());
            if (this.tcpSockets.containsKey(nickname)) nickname = nickname + tcpSocket.getPort();
            this.tcpSockets.put(nickname, tcpSocket);
            this.tcpWriters.put(nickname, writer);
        } finally {
            this.tcpLock.unlock();
        }
        return nickname;
    }

    public void removeConnection(String nickname) {
        try {
            this.tcpLock.lock();
            this.tcpSockets.get(nickname).close();
            this.tcpSockets.remove(nickname);
            this.tcpWriters.remove(nickname);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.tcpLock.unlock();
        }
    }
}
