package no.ntnu.controlpanel;

import no.ntnu.endclients.ClientHandler;
import no.ntnu.tools.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static no.ntnu.greenhouse.GreenhouseSimulator.PORT_NUMBER;
import static no.ntnu.run.ControlPanelStarter.SERVER_HOST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;


public class ControlPanelSocket implements CommunicationChannel {

    private final ControlPanelLogic logic;
    private Socket socket;
    private BufferedReader socketReader;
    private PrintWriter socketWriter;
    private boolean isConnected = false;
    private static KeyPair keyPair;

    private PublicKey clientPublicKey;

    /**
     * Creates an instance of ControlPanelSocket.
     *
     * @param logic The application logic class.
     */
    public ControlPanelSocket(ControlPanelLogic logic) {
        this.logic = logic;
        generateKeyPair();
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
            String encryptedCommand = encryptCommand(command);
            if (encryptedCommand != null) {
                socketWriter.println(encryptedCommand);
                String response = socketReader.readLine();
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



    @Override
    public boolean open() {
        try {
            socket = new Socket(SERVER_HOST, PORT_NUMBER);
            socketWriter = new PrintWriter(socket.getOutputStream(), true);
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String encodedPublicKey = socketReader.readLine();
            byte[] publicKeyBytes = Base64.getDecoder().decode(encodedPublicKey);
            this.clientPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            Logger.info("Successfully connected to: " + SERVER_HOST + ":" + PORT_NUMBER);

            getNodes();
            continuousSensorUpdate();
            isConnected = true;
            return true;
        } catch (IOException e) {
            Logger.error("Could not connect to server: " + e.getMessage());
            return false;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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
        String encryptedCommand = encryptCommand("getNodes");
        socketWriter.println(encryptedCommand);
        Logger.info("Requesting nodes from server...");
        String nodes;
        try {
            nodes = socketReader.readLine();
            System.out.println("Nodes" + nodes);
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
        String encryptedCommand = encryptCommand("updateSensor");
        socketWriter.println(encryptedCommand);
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
            }
        }, 0, 1000);
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String encryptCommand(String command) {
        String encryptedMessage = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);

            byte[] encryptedBytes = cipher.doFinal(command.getBytes());

            encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes);

            //Logger.info("Encrypted Message: " + encryptedMessage);

            return encryptedMessage;
        } catch (Exception e) {
            Logger.error("Error encrypting the command: " + e.getMessage());
            return encryptedMessage;
        }
    }
}
