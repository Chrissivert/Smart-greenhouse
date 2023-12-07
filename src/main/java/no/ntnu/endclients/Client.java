package no.ntnu.endclients;

import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.SensorActuatorNodeInfo;
import no.ntnu.greenhouse.Actuator;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static no.ntnu.tools.Parser.parseIntegerOrError;

public class Client implements CommunicationChannel {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final ControlPanelLogic logic;
    private BufferedReader serverIn;
    private final int TARGET_NODE_PORT = 1234;

    public Client(ControlPanelLogic logic) {
        this.logic = logic;

        try {
            // Opprett en socket-tilkobling til målnoden
            socket = new Socket("localhost", TARGET_NODE_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Create a thread to listen for server updates
            Thread serverListener = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverIn.readLine()) != null) {
                        // Process the server message and update node information locally
                        System.out.println("Received update from server: " + serverMessage);
                        int nodeId = logic.extractNodeId(serverMessage);
                        // Update your nodes based on the received message
                        // Example: handleNodeUpdate(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverListener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn) {
        String command = "Actuator" + " " + actuatorId + " " +
            " ON NODE" + " " + nodeId + " " + "turned" + " " + (isOn ? "ON" : "OFF");
        out.println(command);

        //HER MÅ VI SENDE INFORMASJONEN VIDRE SLIK AT HVER CLIENT BLIR OPPDATERT MED ENDRINGENE
//        handleNodeUpdate();
    }

    @Override
    public boolean open() {
        return socket.isConnected();
    }


//    public void spawnNode(String specification, int delay) {
//        //Dummy method to simulate node addition
//        SensorActuatorNodeInfo nodeInfo = createSensorNodeInfoFrom(specification);
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {logic.onNodeAdded(nodeInfo);
//                int nodeId = nodeInfo.getId();
//                broadcastNodeAdded(nodeId);
////                logic.onNodeRemoved(nodeId);
//            }
//        }, delay * 1000L);
//    }


    private void broadcastNodeAdded(int nodeId) {
        String command = "NodeAdded" + " " + nodeId; // Custom format for node addition
        out.println(command); // Sending the message to the server
    }

}

