package no.ntnu.endclients;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args) {
        int port = 1234;
        Server server = new Server();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                server.clientWriters.add(writer);

                Thread clientThread = new Thread(() -> {
                    try (InputStream in = clientSocket.getInputStream()) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String message;
                        while ((message = reader.readLine()) != null) {
                            System.out.println("Received message from client: " + message);
                            server.broadcast(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }
}
