package no.ntnu.endclients;

import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.SensorActuatorNodeInfo;
import no.ntnu.greenhouse.Actuator;

import java.io.*;
import java.net.Socket;
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
    public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn, String type) {
        String command = "Actuator" + " " + actuatorId + " " + type +
            " ON NODE" + " " + nodeId + " " + "turned" + " " + (isOn ? "ON" : "OFF");
        out.println(command);

        //HER MÅ VI SENDE INFORMASJONEN VIDRE SLIK AT HVER CLIENT BLIR OPPDATERT MED ENDRINGENE
//        handleNodeUpdate();
    }

    @Override
    public boolean open() {
        return socket.isConnected();
    }

    public void close() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void spawnNode(String specification, int delay) {
        //Dummy method to simulate node addition
        SensorActuatorNodeInfo nodeInfo = createSensorNodeInfoFrom(specification);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {logic.onNodeAdded(nodeInfo);
                int nodeId = nodeInfo.getId();
                broadcastNodeAdded(nodeId);
            }
        }, delay * 1000L);
    }

    private void handleNodeUpdate(String serverMessage) {
         logic.checkWhatChanged(serverMessage);
    }

    private void broadcastNodeAdded(int nodeId) {
        String command = "NodeAdded" + " " + nodeId; // Custom format for node addition
        out.println(command); // Sending the message to the server
    }

//    public void removeNode (int nodeId) {
//        logic.onNodeRemoved(nodeId); // Notify ControlPanelLogic about node removal
//
//        // Add code here to inform the server about node removal
//            broadcastNodeRemoved(nodeId); // Function to send node removal info to the server
//    }

    private void broadcastNodeRemoved(int nodeId) {
        // Similar to broadcastNodeAdded, create a method to send node removal info
        String command = "NodeRemoved" + " " + nodeId; // Custom format for node removal
        out.println(command); // Sending the message to the server
    }

    private SensorActuatorNodeInfo createSensorNodeInfoFrom(String specification) {
        if (specification == null || specification.isEmpty()) {
            throw new IllegalArgumentException("Node specification can't be empty");
        }
        String[] parts = specification.split(";");
        if (parts.length > 3) {
            throw new IllegalArgumentException("Incorrect specification format");
        }
        int nodeId = parseIntegerOrError(parts[0], "Invalid node ID:" + parts[0]);
        SensorActuatorNodeInfo info = new SensorActuatorNodeInfo(nodeId);
        if (parts.length == 2) {
            parseActuators(parts[1], info);
        }
        return info;
    }

    private void parseActuators(String actuatorSpecification, SensorActuatorNodeInfo info) {
        String[] parts = actuatorSpecification.split(" ");
        for (String part : parts) {
            parseActuatorInfo(part, info);
        }
    }

    private void parseActuatorInfo(String s, SensorActuatorNodeInfo info) {
        String[] actuatorInfo = s.split("_");
        if (actuatorInfo.length != 2) {
            throw new IllegalArgumentException("Invalid actuator info format: " + s);
        }
        int actuatorCount = parseIntegerOrError(actuatorInfo[0],
                "Invalid actuator count: " + actuatorInfo[0]);
        String actuatorType = actuatorInfo[1];
        for (int i = 0; i < actuatorCount; ++i) {
            Actuator actuator = new Actuator(actuatorType, info.getId());
            actuator.setListener(logic);
            info.addActuator(actuator);
        }
    }
}
