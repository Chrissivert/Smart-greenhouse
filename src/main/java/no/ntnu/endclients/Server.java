package no.ntnu.endclients;

import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelLogic;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements CommunicationChannel {

    private ControlPanelLogic logic;

    private List<ClientHandler> clients = new ArrayList<>();

    public Server() {
    }

    public void setLogic(ControlPanelLogic logic) {
        this.logic = logic;
    }

    public static void main(String[] args) throws IOException {
        int port = 1234;
        Server server = new Server();

        ControlPanelLogic logic = new ControlPanelLogic();
        server.setLogic(logic);

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

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    @Override
    public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn, String type) {
        String state = isOn ? "ON" : "off";
        String message = "actuator " + state + " " + type + " " + actuatorId + " " + nodeId;
       // broadcastMessage(message);
    }

    @Override
    public boolean open() {
        return false;
    }
}
