package no.ntnu.controlpanel;

import no.ntnu.tools.EncrypterDecrypter;
import no.ntnu.tools.Logger;

import static no.ntnu.greenhouse.GreenhouseSimulator.SERVER_PORT_NUMBER;
import static no.ntnu.run.ControlPanelStarter.SERVER_HOST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;


/**
 * IMPORANT! Much of this code is the same as the
 * The socket of a controlPanel. It uses a communication channel to send commands
 * and receive events.
 */

public class ControlPanelSocket extends Thread implements CommunicationChannel {

    private final ControlPanelLogic logic;
    private Socket socket;
    private BufferedReader socketReader;
    private PrintWriter socketWriter;
    private boolean isConnected = false;
    private volatile boolean readLineIsLocked;

    /**
     * Creates an instance of ControlPanelSocket.
     *
     * @param logic The application logic class.
     */
    public ControlPanelSocket(ControlPanelLogic logic) {
        this.logic = logic;
        this.readLineIsLocked = false;
    }

    /**
     * Runs the thread that handles messages sent from the server, continuously reading.
     * Handles the incoming commands and responds accordingly.
     */
    public synchronized void runThread() {
        try {
            String inputLine;
            if (socketReader.ready() && !readLineIsLocked) {
                inputLine = socketReader.readLine();
                String input = EncrypterDecrypter.decryptMessage(inputLine);
                handleInput(input);
            }
        } catch (IOException e) {
            Logger.error("while reading from the socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the processing of a raw command, taking appropriate actions based on the command's content.
     *
     * @param rawCommand The command as a string
     */
    private void handleInput(String rawCommand) {
        if (rawCommand == null) {
            return;
        }
        if (rawCommand.equals("updateNodes")) {
            getNodes();
        }
        if (rawCommand.contains("updateActuatorStates")) {
            this.updateActuatorStates(rawCommand);
        }
    }

    /**
     * This method should open the connection to the server.
     *
     * @return True if connection is successfully opened, false on error.
     */
    @Override
    public void open() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT_NUMBER);
            socketWriter = new PrintWriter(socket.getOutputStream(), true);
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Logger.info("Successfully connected to: " + SERVER_HOST + ":" + SERVER_PORT_NUMBER);

            getNodes();
            continuousSensorUpdate();
            isConnected = true;
        } catch (IOException e) {
            Logger.error("Could not connect to server: " + e.getMessage());
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
                        "Connection with client: " + SERVER_HOST + ":" + SERVER_PORT_NUMBER + " has been closed");
            }
        } catch (IOException e) {
            Logger.error("Could not close connection: " + e.getMessage());
        }
    }

    /**
     * Performs socket communication by first sending, then receiving a message.
     *
     * @param sendMessage the message to be sent
     * @return the messaged received, or null if an error occurs
     */
    private String performSocketCommunication(String sendMessage) {
        String receiveMessage = null;

        try {
            String encryptedCommand = EncrypterDecrypter.encryptMessage(sendMessage);
            if (encryptedCommand != null) {
                //"Lock" the readLine function, so the other thread cannot read the line until this method is done running.
                this.readLineIsLocked = true;
                socketWriter.println(encryptedCommand);
                receiveMessage = socketReader.readLine();
                this.readLineIsLocked = false;

                receiveMessage = EncrypterDecrypter.decryptMessage(receiveMessage);
            } else {
                Logger.error("Error encrypting the command.");
            }
        } catch (IOException e) {
            Logger.error("An unexpected error occurred: " + e.getMessage());
        }
        //In case of an exception, make sure it is unlocked
        this.readLineIsLocked = false;

        return receiveMessage;
    }

    /**
     * This method should get all nodes from server, and add them to
     * the controlPanel.
     */
    public void getNodes() {
        Logger.info("Requesting nodes from server...");
        String nodes = performSocketCommunication("getNodes");

        //Does not contain a ";" if there are no nodes. base64 also does not have the symbol, so this catches decryption errors
        if (!nodes.contains(";")) {
            Logger.info("Nodes not loaded, since no nodes received");

        } else if (nodes.contains("=")) {
            Logger.info("Where dose this come from?"); //DEBUG
        } else {
            String[] nodeList = nodes.split("/");

            for (String node : nodeList) {
                logic.onNodeAdded(logic.createSensorNodeInfoFrom(node));
            }
            Logger.info("Nodes loaded");
        }
    }

    /**
     * This method should send a command to a specific actuator
     *
     * @param nodeId     ID of the node to which the actuator is attached
     * @param actuatorId Node-wide unique ID of the actuator
     * @param isOn       When true, actuator must be turned on; off when false.
     */
    public void sendActuatorChange(int actuatorId, int nodeId, boolean isOn) {
        Logger.info("Sending command to actuator " + nodeId + " on node " + actuatorId);
        String on = isOn ? "0" : "1";
        String command = actuatorId + ", " + nodeId + ", " + on;

        String response = performSocketCommunication(command);
        Logger.info(response);

    }

    private void updateActuatorStates(String rawCommand){
        if(rawCommand.contains("updateActuatorStates")) {
            Logger.info(rawCommand + " received");
            String nodeStateInfo = rawCommand.replace("updateActuatorStates:", "");
            String[] nodeStateInfoList = nodeStateInfo.split(",");
            int actuatorId = Integer.parseInt(nodeStateInfoList[0]);
            int nodeId = Integer.parseInt(nodeStateInfoList[1]);
            boolean state = nodeStateInfoList[2].equals("ON");
            Logger.info("Actuator " + actuatorId + " on node " + nodeId + " is now " + state + "." + "    aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            logic.onActuatorStateChangedButNotReally(nodeId, actuatorId, state);
        }
    }

    /**
     * This method should update the sensors continually.
     */
    public void updateSensorData() {
        String sensors = performSocketCommunication("updateSensor");
    }

    /**
     * This method sends requests to the server for sensor updates every 1 second.
     */
    public void continuousSensorUpdate() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateSensorData();
            }
        }, 0, 1000);
    }

    /**
     * This method should return true if the socket is open, false otherwise.
     *
     * @return true if the socket is open, false otherwise.
     */
    public boolean isOpen() {
        return this.isConnected;
    }
}
