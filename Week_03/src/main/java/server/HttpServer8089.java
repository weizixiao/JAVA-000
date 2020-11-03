package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer8089 {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8089);
        // 开启线程池
        ExecutorService executorService = Executors.newFixedThreadPool(40);
        while (true) {
            try {
                final Socket socket = serverSocket.accept();
                executorService.execute(() -> service(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void service(Socket socket) {
        try {
            String html = "hello,park";
            Thread.sleep(20);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.println("HTTP/1.1 200 OK");
            // 不加Length，浏览器打不开
            printWriter.println("Content-Length: " + html.length());
            printWriter.println("Content-Type: text/html; charset=utf-8");
            printWriter.println();
            printWriter.write(html);
            printWriter.println();
            printWriter.close();
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
