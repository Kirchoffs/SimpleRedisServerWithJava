import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

public class RedisServer {
    public static void main(String[] args){
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
        ConcurrentMap<String, Long> expire = new ConcurrentHashMap<>();
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);

            System.out.println("Server starts...");
            
            while (true) {
                clientSocket = serverSocket.accept();
                executorService.execute(new ClientHandler(clientSocket, cache, expire));
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}
