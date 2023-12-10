package no.ntnu.controlpanel;

import no.ntnu.tools.EncrypterDecrypter;
import no.ntnu.tools.Logger;

import static no.ntnu.greenhouse.GreenhouseSimulator.PORT_NUMBER;
import static no.ntnu.run.ControlPanelStarter.SERVER_HOST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;


/**
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
     * Runs the client handler thread, continuously reading messages from the client.
     * Handles the incoming commands and responds accordingly.
     */
//    @Override
    public synchronized void run1() {
        try {
            String inputLine;
            if(socketReader.ready() && !readLineIsLocked) {
                inputLine = socketReader.readLine();
                String input = EncrypterDecrypter.decryptMessage(inputLine);
                handleInput(input);
//              writer.println(EncrypterDecrypter.encryptMessage(a));
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
        if(rawCommand == null){
            return;
        }
        if (rawCommand.equals("updateNodes")) {
            updateNodes();
        } else {
            System.out.println(rawCommand);
        }
    }

    private void updateNodes() {
        System.out.println("test");
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
        try {
            String encryptedCommand = EncrypterDecrypter.encryptMessage(command);
            if (encryptedCommand != null) {
                //"Lock" the readLine function, so the other thread cannot read the line until this method is done running.
                this.readLineIsLocked = true;
                socketWriter.println(encryptedCommand);
                String response = socketReader.readLine();
                //Unlock.
                this.readLineIsLocked = false;
                Logger.info(response);
            } else {
                Logger.error("Error encrypting the command.");
            }
        } catch (IOException e) {
            Logger.error("Error sending command to actuator " + actuatorId + " on node " + nodeId + ": " +
                    e.getMessage());
        } catch (Exception e) {
            Logger.error("An unexpected error occurred: " + e.getMessage());
        }
    }


    /**
     * This method should open the connection to the server.
     *
     * @return True if connection is successfully opened, false on error.
     */

    @Override
    public boolean open() {
        try {
            socket = new Socket(SERVER_HOST, PORT_NUMBER);
            socketWriter = new PrintWriter(socket.getOutputStream(), true);
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Logger.info("Successfully connected to: " + SERVER_HOST + ":" + PORT_NUMBER);

            getNodes();
            continuousSensorUpdate();
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
        String encryptedCommand = EncrypterDecrypter.encryptMessage("getNodes");
        //"Lock" the readLine function, so the other thread cannot read the line until this method is done running.
        this.readLineIsLocked = true;
        socketWriter.println(encryptedCommand);
        Logger.info("Requesting nodes from server...");
        String nodes;
        try {
            nodes = EncrypterDecrypter.decryptMessage(socketReader.readLine());
            System.out.println(nodes);
            System.out.println("Nodes" + nodes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Unlock.
        this.readLineIsLocked = false;

        if (nodes.equals("null")) {
            Logger.info("Nodes not loaded, since no nodes received");

        } else {
            String[] nodeList = nodes.split("/");

            for (String node : nodeList) {
                logic.onNodeAdded(logic.createSensorNodeInfoFrom(node));
            }
            Logger.info("Nodes loaded");
        }
    }

    /**
     * This method should update the sensors continually.
     */
    public void updateSensorData() {
        String encryptedCommand = EncrypterDecrypter.encryptMessage("updateSensor");
        //"Lock" the readLine function, so the other thread cannot read the line until this method is done running.
        this.readLineIsLocked = true;
        socketWriter.println(encryptedCommand);
        String sensors = "";
        try {
            sensors = EncrypterDecrypter.decryptMessage(socketReader.readLine());
            System.out.println(sensors);
        } catch (IOException e) {
            Logger.info("Stopping sensor reading");
        }
        //Unlock.
        this.readLineIsLocked = false;
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

}
