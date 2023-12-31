package no.ntnu.greenhouse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.ntnu.endclients.ClientHandler;
import no.ntnu.gui.greenhouse.ButtonActionHandler;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.tools.Logger;

import static java.lang.Thread.sleep;

/**
 * Application entrypoint - a simulator for a greenhouse.
 * Works essentially as a server.
 */
public class GreenhouseSimulator {

    /**
     * The default port number for the server.
     */
    public static final int SERVER_PORT_NUMBER = 1234;
    private ServerSocket serverSocket;

    /**
     * A map of all the nodes in the greenhouse.
     */
    public static final Map<Integer, SensorActuatorNode> nodes = new HashMap<>();

    private final List<PeriodicSwitch> periodicSwitches = new LinkedList<>();

    private final List<ClientHandler> connectedClients = new ArrayList<>();

    private boolean isServerRunning;


    /**
     * Create a greenhouse simulator.
     *
     * @param fake When true, simulate a fake periodic events instead of creating
     *             socket communication
     */
    public GreenhouseSimulator(boolean fake) {
        new ButtonActionHandler(this);
    }

    /**
     * Initialise the greenhouse but don't start the simulation just yet.
     */
    public void initialize() {
        createNode(1, 2, 1, 0, 1);
        createNode(1, 0, 0, 2, 1);
        createNode(2, 0, 0, 0, 0);
        Logger.info("Greenhouse initialized");
    }

    /**
     * Create a new sensor/actuator node with the specified configuration.
     *
     * @param temperature Initial temperature value
     * @param humidity    Initial humidity value
     * @param windows     Initial window count
     * @param fans        Initial fan count
     * @param heaters     Initial heater count
     */
    public void createNode(int temperature, int humidity, int windows, int fans, int heaters) {
        SensorActuatorNode node = DeviceFactory.createNode(
                temperature, humidity, windows, fans, heaters);
        System.out.println("Created node " + node.getId());
        nodes.put(node.getId(), node);
    }

    /**
     * Start a simulation of a greenhouse - all the sensor and actuator nodes inside it.
     */
    public void start() {
        initiateCommunication();
        for (SensorActuatorNode node : nodes.values()) {
            node.start();
        }
        for (PeriodicSwitch periodicSwitch : periodicSwitches) {
            periodicSwitch.start();
        }

        Logger.info("Simulator started");
    }


    /**
     * Initiates the communication between the server and the client.
     */
    private void initiateCommunication() {
        Thread serverThread = new Thread(this::initiateRealCommunication);
        serverThread.start();
    }

    /**
     * Add a new node to the greenhouse.
     *
     * @param newNode The node to add
     */
    public void addNode(SensorActuatorNode newNode) {
        nodes.put(newNode.getId(), newNode);
        this.connectedClients.forEach(ClientHandler::updateNodes);
    }

    /**
     * Stop the simulation of the greenhouse - all the nodes in it.
     */
    public void stop() {
        stopCommunication();
        nodes.values().forEach(SensorActuatorNode::stop);
    }

    /**
     * Stop the communication between the server and the client.
     */

    private void stopCommunication() {
        try {
            serverSocket.close();
            Logger.info("TCP connection successfully closed");
        } catch (IOException e) {
            Logger.error("An error occurred while stopping communication");
        }
    }

    /**
     * Initiates the real communication between the server and the client.
     */

    private void initiateRealCommunication() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT_NUMBER);

            Logger.info("Server is now listening on port " + SERVER_PORT_NUMBER);
        } catch (IOException e) {
            Logger.error("TCP connection not established due to error : " + e.getMessage());
            return;
        }
        isServerRunning = true;
        while (isServerRunning && !serverSocket.isClosed()) {
            ClientHandler clientHandler = acceptNextClientConnection(serverSocket);

            if (clientHandler != null) {
                addClientToConnectedClients(clientHandler);
                clientHandler.start();
            }
        }
    }

    /**
     * Accepts a new client connection.
     *
     * @param listeningSocket The socket to listen for new connections on
     * @return A client handler for the new client
     */

    private ClientHandler acceptNextClientConnection(ServerSocket listeningSocket) {
        try {
            Socket clientSocket = listeningSocket.accept();
            Logger.info("New client connected from " + clientSocket.getRemoteSocketAddress());
            return new ClientHandler(clientSocket, this);
        } catch (IOException e) {
            Logger.error("Could not accept client connection: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handle actuator commands received from the client.
     *
     * @param actuatorId The ID of the actuator to handle
     * @param nodeId     The ID of the node containing the actuator
     * @param isOn       Whether to turn the actuator on or off
     */
    public void handleActuator(int actuatorId, int nodeId, boolean isOn) {
        if (!isOn) {
            nodes.get(nodeId).getActuators().get(actuatorId).turnOn();
        } else {
            nodes.get(nodeId).getActuators().get(actuatorId).turnOff();
        }
        broadcastActuatorStateChange(actuatorId, nodeId, isOn);
    }

    /**
     * Broadcast a change in actuator state to all connected clients.
     *
     * @param actuatorId The ID of the actuator
     * @param nodeId     The ID of the node containing the actuator
     * @param isOn       Whether the actuator is on or off
     */
    public void broadcastActuatorStateChange(int actuatorId, int nodeId, boolean isOn) {
        for (ClientHandler client : connectedClients) {
            client.updateActuatorStates(actuatorId, nodeId, isOn);
        }
    }

    /**
     * Handle a command to turn on or off all actuators in a node.
     *
     * @param nodeId The ID of the node containing the actuators
     * @param isOn   Whether to turn the actuators on or off
     */
    public void handleAllActuators(int nodeId, boolean isOn) {
        for (Actuator actuator : nodes.get(nodeId).getActuators()) {
            actuator.set(isOn);
            broadcastActuatorStateChange(actuator.getId(), nodeId, !isOn); //boolean isOn is inverted, dont know why, but it works
        }
    }

    /**
     * Retrieve a formatted string representing the state of actuators in the greenhouse.
     *
     * @return A formatted string containing actuator information
     */
    public String getNodes() {
        Map<Integer, List<Actuator>> actuatorsByNode = new HashMap<>();
        for (SensorActuatorNode node : nodes.values()) {
            for (Actuator actuator : node.getActuators()) {
                actuatorsByNode.computeIfAbsent(actuator.getNodeId(), k -> new ArrayList<>()).add(actuator);
            }
        }

        List<String> commands = new ArrayList<>();
        for (Map.Entry<Integer, List<Actuator>> entry : actuatorsByNode.entrySet()) {
            int nId = entry.getKey();
            List<Actuator> actuators = entry.getValue();

            String actuatorString = actuators.stream()
                    .map(a -> a.getId() + "_" + a.getType())
                    .collect(Collectors.joining(" "));

            String commandString = nId + ";" + actuatorString;
            commands.add(commandString);
        }

        return String.join("/", commands);
    }

    /**
     * Updates all sensors and generates commands for each sensor node.
     * Work in progress.
     *
     * @return A string containing commands for sensor nodes.
     */
    public String updateSensors() {
        Map<Integer, List<Sensor>> sensorsByNode = new HashMap<>();

        for (SensorActuatorNode node : nodes.values()) {
            for (Sensor sensor : node.getSensors()) {
                sensorsByNode.computeIfAbsent(node.getId(), k -> new ArrayList<>()).add(sensor);
            }
        }

        List<String> commands = new ArrayList<>();

        for (Map.Entry<Integer, List<Sensor>> entry : sensorsByNode.entrySet()) {
            int nodeId = entry.getKey();
            List<Sensor> sensors = entry.getValue();

            String actuatorString = sensors.stream()
                    .map(sensor -> String.valueOf(sensor.getReading()))
                    .collect(Collectors.joining(" "));

            String commandString = nodeId + ";" + actuatorString;
            commands.add(commandString);
        }

        return formatSensorCommand(String.join("/", commands));
    }

    /**
     * Format a command string for sensor nodes by removing unnecessary characters.
     *
     * @param command The original command string
     * @return The formatted command string
     */
    public String formatSensorCommand(String command) {
        return command.replace("{", "").replace("}", "")
                .replace(",", "").replace("   ", ",")
                .replace("type=", "").replace(" value", "")
                .replace("unit=", "").replace("; ", ";");
    }

    /**
     * Remove a disconnected client from the list of connected clients.
     *
     * @param clientHandler The client handler to remove
     */
    public void removeDisconnectedClient(ClientHandler clientHandler) {
        connectedClients.remove(clientHandler);
    }

    /**
     * Add a client to the list of connected clients.
     *
     * @param clientHandler The client handler to add
     */

    private void addClientToConnectedClients(ClientHandler clientHandler) {
        connectedClients.add(clientHandler);
    }

    /**
     * Add a listener for notification of node staring and stopping.
     *
     * @param listener The listener which will receive notifications
     */
    public void subscribeToLifecycleUpdates(NodeStateListener listener) {
        for (SensorActuatorNode node : nodes.values()) {
            node.addStateListener(listener);
        }
    }

    /**
     * Returns a map of all the nodes in the greenhouse.
     *
     * @return A map of all the nodes in the greenhouse.
     */

    public Map<Integer, SensorActuatorNode> getMap() {
        return nodes;
    }
}
