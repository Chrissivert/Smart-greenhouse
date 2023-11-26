package no.ntnu.endclients;

import no.ntnu.controlpanel.ControlPanelLogic;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ControlPanelLogic logic; // Not static anymore

    public Server() {
        // You can initialize logic here if needed
    }

    // Setter method to set the logic instance
    public void setLogic(ControlPanelLogic logic) {
        this.logic = logic;
    }

    public static void main(String[] args) throws IOException {
        int port = 1234;
        Server server = new Server();

        ControlPanelLogic logic = new ControlPanelLogic(); // Instantiate ControlPanelLogic
        server.setLogic(logic); // Set the logic instance in the server

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler clientHandler = new ClientHandler(socket, server, logic);
                Thread thread = new Thread(clientHandler); // Start a new thread for this client
                thread.start();
            }
        }
    }
    public void addClient(ClientHandler clientHandler) {

    }

}

//    private void broadcast(String message) {
//        for (PrintWriter writer : clientWriters) {
//            writer.println(message);
//        }
//    }

//    public void startServer() {
//        // Code to start the server socket and accept incoming connections
//        // For each incoming connection, create a ClientHandler instance
//    }


//    public void addClient(ClientHandler clientHandler) {
//
//    }

