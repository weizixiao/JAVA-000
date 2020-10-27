package nio;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer03 {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8803);
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
            System.out.println("=============");
            Thread.sleep(20);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.println("HTTP/1.1 200 OK");
            // 不加Length，浏览器打不开
            printWriter.println("Content-Length: 9");
            printWriter.println("Content-Type: text/html; charset=utf-8");
            printWriter.println();
            printWriter.write("hello,nio");
            printWriter.println();
            printWriter.close();
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
