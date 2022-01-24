import java.net.ServerSocket;
import java.net.Socket;


public class HTTPServer {

    static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        ServerSocket serverConnect = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
        int id = 1;
        while (true) {
            Socket s = serverConnect.accept();
            Worker wt = new Worker(s, id);
            Thread t = new Thread(wt);
            t.start();
            //System.out.println("Client " + id + " connected to server");
            id++;
        }
    }
}
