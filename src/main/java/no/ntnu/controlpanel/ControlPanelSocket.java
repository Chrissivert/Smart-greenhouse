package no.ntnu.controlpanel;

import no.ntnu.tools.Logger;

import static no.ntnu.greenhouse.GreenhouseSimulator.PORT_NUMBER;
import static no.ntnu.run.ControlPanelStarter.SERVER_HOST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


public class ControlPanelSocket implements CommunicationChannel {

    private final ControlPanelLogic logic;
    private Socket socket;
    private BufferedReader socketReader;
    private PrintWriter socketWriter;
    private boolean isConnected = false;

    /**
     * Creates an instance of ControlPanelSocket.
     *
     * @param logic The application logic class.
     */
    public ControlPanelSocket(ControlPanelLogic logic) {
        this.logic = logic;
    }

    /**
     * This method should send a command to a specific actuator
     *
     * @param nodeId     ID of the node to which the actuator is attached
     * @param actuatorId Node-wide unique ID of the actuator
     * @param isOn       When true, actuator must be turned on; off when false.
     */
    @Override
    public void sendActuatorChange(int actuatorId, int nodeId, boolean isOn) {
        Logger.info("Sending command to actuator " + nodeId + " on node " + actuatorId);
        String on = isOn ? "0" : "1";
        String command = actuatorId + ", " + nodeId + ", " + on;

        try {
            socketWriter.println(command);
            String response = socketReader.readLine();
            Logger.info(response);
        } catch (IOException e) {
            Logger.error("Error sending command to actuator " + actuatorId + " on node " + nodeId + ": " +
                    e.getMessage());
        }
    }

    @Override
    public boolean open() {
        try {
            socket = new Socket(SERVER_HOST, PORT_NUMBER);
            socketWriter = new PrintWriter(socket.getOutputStream(), true);
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Logger.info("Successfully connected to: " + SERVER_HOST + ":" + PORT_NUMBER);

            continuousSensorUpdate();
            getNodes();
            isConnected = true;
            return true;
        } catch (IOException e) {
            Logger.error("Could not connect to server: " + e.getMessage());
            return false;
        }
    }

    /**
     * This method should close the connection to the server.
     */
    public void close() {
        try {
            if (isConnected) {
                socket.close();
                socketWriter.close();
                socketReader.close();
                Logger.info(
                        "Connection with client: " + SERVER_HOST + ":" + PORT_NUMBER + " has been closed");
            }
        } catch (IOException e) {
            Logger.error("Could not close connection: " + e.getMessage());
        }
    }

    /**
     * This method should get all nodes from server, and add them to
     * the controlPanel.
     */
    public void getNodes() {
        socketWriter.println("getNodes");
        Logger.info("Requesting nodes from server...");
        String nodes;
        try {
            nodes = socketReader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String[] nodeList = nodes.split("/");
        for (String node : nodeList) {
            logic.onNodeAdded(logic.createSensorNodeInfoFrom(node));
        }
        Logger.info("Nodes loaded");
    }

    /**
     * This method should update the sensors continually.
     */
    public void updateSensorData() {
        socketWriter.println("updateSensor");
        String sensors = "";
        try {
            sensors = socketReader.readLine();
        } catch (IOException e) {
            Logger.info("Stopping sensor reading");
        }
    }

    /**
     * This method sends requests to the server for sensor updates every 2 seconds.
     */
    public void continuousSensorUpdate() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateSensorData();
                logic.actuatorTurnOnAllActuators();
            }
        }, 0, 5500);
    }

}
