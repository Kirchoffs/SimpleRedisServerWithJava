import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.Socket;

import java.util.concurrent.ConcurrentMap;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ConcurrentMap<String, String> cache;
    private ConcurrentMap<String, Long> expire;
    
    public ClientHandler(Socket clientSocket, 
                         ConcurrentMap<String, String> cache, 
                         ConcurrentMap<String, Long> expire) {
        this.clientSocket = clientSocket;
        this.cache = cache;
        this.expire = expire;
    }

    public void run() {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));

            String res = null;
            while ((res = in.readLine()) != null) {
                int cnt = Integer.parseInt(res.substring(1));
                in.readLine(); // digest length of op
                String op = in.readLine();
                cnt--;
                if (op.equalsIgnoreCase("PING")) {
                    out.format("+PONG\r\n");
                } else if (op.equalsIgnoreCase("ECHO")) {
                    in.readLine(); // digest length of echo string
                    out.format("+" + in.readLine() + "\r\n");
                } else if (op.equalsIgnoreCase("SET")) {
                    in.readLine();
                    String key = in.readLine();
                    cnt--;
                    in.readLine();
                    String val = in.readLine();
                    cnt--;
                    cache.put(key, val);

                    if (cnt > 0) {
                        in.readLine();
                        in.readLine();
                        in.readLine();
                        int milliseconds = Integer.parseInt(in.readLine());
                        expire.put(key, System.currentTimeMillis() + milliseconds);
                    }

                    out.format("+OK\r\n");
                } else if (op.equalsIgnoreCase("GET")) {
                    in.readLine();
                    String key = in.readLine();
                    String val = cache.getOrDefault(key, null);
                    if (val != null) {
                        if (expire.getOrDefault(key, Long.MAX_VALUE) < System.currentTimeMillis()) {
                            cache.remove(key);
                            expire.remove(key);
                            out.format("$-1\r\n");
                        } else {
                            out.format("$" + val.length() + "\r\n" + val + "\r\n");
                        }
                    } else {
                        out.format("$-1\r\n");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}