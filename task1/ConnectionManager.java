import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

public class ConnectionManager {
    private final HashMap<String, Socket> sockets = new HashMap<>();
    private final HashMap<String, PrintWriter> writers = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void forEveryConnection(BiConsumer<String, PrintWriter> callback) {
        try {
            this.lock.lock();
            for (Map.Entry<String, PrintWriter> entry : this.writers.entrySet()) {
                callback.accept(entry.getKey(), entry.getValue());
            }
        } finally {
            this.lock.unlock();
        }
    }

    public void forEverySocket(BiConsumer<String, Socket> callback) {
        try {
            this.lock.lock();
            for (Map.Entry<String, Socket> entry : this.sockets.entrySet()) {
                callback.accept(entry.getKey(), entry.getValue());
            }
        } finally {
            this.lock.unlock();
        }
    }

    public String addConnection(String nickname, Socket tcpSocket) throws IOException {
        try {
            this.lock.lock();
            PrintWriter writer = new PrintWriter(tcpSocket.getOutputStream(), true);
            writer.println(tcpSocket.getPort());
            if (this.sockets.containsKey(nickname)) nickname = nickname + tcpSocket.getPort();
            this.sockets.put(nickname, tcpSocket);
            this.writers.put(nickname, writer);
        } finally {
            this.lock.unlock();
        }
        return nickname;
    }

    public void removeConnection(String nickname) {
        try {
            this.lock.lock();
            this.sockets.get(nickname).close();
            this.sockets.remove(nickname);
            this.writers.remove(nickname);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.lock.unlock();
        }
    }
}
