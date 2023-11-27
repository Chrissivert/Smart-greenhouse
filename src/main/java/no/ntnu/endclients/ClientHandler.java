package no.ntnu.endclients;

import no.ntnu.controlpanel.ControlPanelLogic;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ControlPanelLogic logic;
    private final Server server;

    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket, Server server, ControlPanelLogic logic) {
        this.socket = socket;
        this.server = server;
        this.logic = logic;
        server.addClient(this);

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Client on port: " + socket.getPort() + " is connected");
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                System.out.println("Client on port " + socket.getPort() + " sent message: " + inputLine);
                server.broadcastMessage("hello clients");
                writer.println("Server: " + inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        System.out.println("Sending message: " + message);
        writer.println(message);
        System.out.println("Message sent to client on port " + socket.getPort());
    }
}
