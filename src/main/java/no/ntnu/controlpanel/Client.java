package no.ntnu.controlpanel;

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

    private final int TARGET_NODE_PORT = 1234;

    public Client(ControlPanelLogic logic) {
        this.logic = logic;

        try {
            // Opprett en socket-tilkobling til målnoden
            socket = new Socket("localhost", TARGET_NODE_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn, String type) {
        // Send en kommando til målnoden
        String command = "Actuator" + " " + actuatorId + " " + type +
            " ON NODE" + " " + nodeId + " " + "turned" + " " + (isOn ? "ON" : "OFF");
        out.println(command);
    }

    @Override
    public boolean open() {
        // Implementer åpning av kommunikasjon (kan være tilkoblingen ovenfor)
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
        SensorActuatorNodeInfo nodeInfo = createSensorNodeInfoFrom(specification);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logic.onNodeAdded(nodeInfo);
            }
        }, delay * 1000L);
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

