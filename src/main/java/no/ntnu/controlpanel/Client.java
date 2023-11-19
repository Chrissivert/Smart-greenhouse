package no.ntnu.controlpanel;

import com.sun.javafx.event.EventHandlerManager;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.listeners.common.ActuatorListener;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import static no.ntnu.tools.Parser.parseIntegerOrError;

public class Client implements CommunicationChannel, ActuatorListener {

    private EventManager eventManager;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final ControlPanelLogic logic;

    private final int TARGET_NODE_PORT = 1234;

    public Client(ControlPanelLogic logic, EventManager eventManager) {
        this.logic = logic;
        this.eventManager = eventManager;
        this.eventManager.subscribe(this);

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
    public void actuatorUpdated(int nodeId, Actuator actuator) {
        // Handle actuator update events here
        // Update the logic or UI of the control panel based on the received actuator change
        logic. onActuatorStateChanged(nodeId, actuator.getId(),actuator.isOn());
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

