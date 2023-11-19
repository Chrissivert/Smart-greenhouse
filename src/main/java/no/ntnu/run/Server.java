package no.ntnu.run;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        int port = 1234;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Thread clientThread = new Thread(() -> {
                        try (InputStream in = clientSocket.getInputStream()) {

                            // Read and process client messages here
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                String message = new String(buffer, 0, bytesRead);
                                // Process the message and take appropriate action
                                System.out.println("Received message from client: " + message);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    clientThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
        }
    }
}