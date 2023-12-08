//package no.ntnu.endclients;
//
//import no.ntnu.controlpanel.CommunicationChannel;
//import no.ntnu.controlpanel.ControlPanelLogic;
//import no.ntnu.greenhouse.GreenhouseSimulator;
//import no.ntnu.greenhouse.SensorActuatorNode;
//import no.ntnu.gui.controlpanel.ControlPanelApplication;
//import no.ntnu.run.ControlPanelStarter;
//
//import java.io.*;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class Server {
//
//    private List<ClientHandler> clients = new ArrayList<>();
//    private final String serverAddress;
//    private final int serverPort;
//
//    public Server(String serverAddress, int serverPort) {
//        this.serverAddress = serverAddress;
//        this.serverPort = serverPort;
//    }
//
//    public void startServer() {
//
//        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
//            System.out.println("Server is listening on port " + serverPort);
//            while (true) {
//                Socket socket = serverSocket.accept();
//                System.out.println("New client connected");
//                ClientHandler clientHandler = new ClientHandler(socket, this);
//                Thread thread = new Thread(clientHandler);
//                thread.start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public synchronized void addClient(ClientHandler client) {
//        clients.add(client);
//    }
//
//    public synchronized void removeClient(ClientHandler client) {
//        clients.remove(client);
//    }
//
//    public void broadcastMessage(String message) {
//        for (ClientHandler clientHandler : clients) {
//            clientHandler.sendMessage(message);
//        }
//    }
//
//    public void sendNodesToServer(Map<Integer, SensorActuatorNode> nodes) {
//        try (Socket socket = new Socket(serverAddress, serverPort)) {
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//            objectOutputStream.writeObject(nodes);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
