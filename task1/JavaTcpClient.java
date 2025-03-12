import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class JavaTcpClient {

    public static void main(String[] args) throws IOException {
        System.out.println("JAVA TCP CLIENT");
        String hostName = "localhost";
        int portNumber = 12345;
        Socket socket = null;
        Scanner keyboard = new Scanner(System.in);

        System.out.println("Please provide a nickname");
        String nickname = keyboard.nextLine();

        try {
            socket = new Socket(hostName, portNumber);

            Socket finalSocket = socket;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    finalSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

            // in & out streams
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(nickname);

            Thread inputHandler = new Thread(() -> {
                while (true) {
                    out.println(keyboard.nextLine());
                }
            });

            inputHandler.start();

            while (true) {
                System.out.println(in.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null){
                socket.close();
            }
        }
    }
}
