package no.ntnu.endclients;

import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.tools.Logger;


import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ClientHandler extends Thread {
    protected Socket socket;
    private final GreenhouseSimulator simulator;
    private BufferedReader reader;
    private PrintWriter writer;
    private KeyPair keyPair;
    private PublicKey socketPublicKey;



    /**
     * Creates a new instance of the ClientHandler class.
     *
     * @param socket    The socket representing the client connection
     * @param simulator The greenhouse simulator associated with this client handler
     */
    public ClientHandler(Socket socket, GreenhouseSimulator simulator) {
        this.socket = socket;
        this.simulator = simulator;
        generateKeyPair();
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the client handler thread, continuously reading messages from the client.
     * Handles the incoming commands and responds accordingly.
     */
    @Override
    public void run() {
        sendPublicKey();
        try {
            readPublicKey();
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try {
            System.out.println("Client on port: " + socket.getPort() + " is connected");
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                String a = decryptMessage(inputLine);
                handleInput(a);
                writer.println(a);
            }
        } catch (IOException e) {
            Logger.error("while reading from the socket: " + e.getMessage());
            e.printStackTrace();
        }
        String clientAddress = socket.getRemoteSocketAddress().toString();
        Logger.info("Client at " + clientAddress + " has disconnected.");
        simulator.removeDisconnectedClient(this);
    }

    private void readPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String encodedPublicKey = reader.readLine();
        byte[] publicKeyBytes = Base64.getDecoder().decode(encodedPublicKey);
        this.socketPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }

    private void sendPublicKey() {
        PublicKey clientPublicKey = getPublicKey();
        String encodedPublicKey = Base64.getEncoder().encodeToString(clientPublicKey.getEncoded());
        writer.println(encodedPublicKey);
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
        if (rawCommand.equals("getNodes")) {
            handleGetNodesCommand();
        } else if (rawCommand.equals("updateSensor")) {
            handleUpdateSensorCommand();
        } else {
            processActuatorCommand(rawCommand);
        }
    }

    private void handleGetNodesCommand() {
       writer.println(encryptCommand(simulator.getNodes()));
    }


    private void handleUpdateSensorCommand() {
        writer.println(encryptCommand(simulator.updateSensors()));
    }


    private void processActuatorCommand(String rawCommand) {
        String[] parts = rawCommand.split(",");
        if (parts.length == 3) {
            int nodeId = Integer.parseInt(parts[0].trim());
            int actuatorId = Integer.parseInt(parts[1].trim());
            int on = Integer.parseInt(parts[2].trim());
            boolean isOn = (on != 0);

            simulator.handleActuator(actuatorId, nodeId, isOn);

            String state = isOn ? "OFF" : "ON";

            writer.println(encryptCommand("  >>> Server response: Actuator[" + actuatorId +
                    "] on node " + nodeId + " is set to " + state));
        } else {
            Logger.error("Incorrect command format: " + rawCommand);
        }
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

    private String decryptMessage(String commandToDecrypt) {
        String decryptedMessage = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(commandToDecrypt));

            decryptedMessage = new String(decryptedBytes);

            //Logger.info("Decrypted Message: " + decryptedMessage);
        } catch (Exception e) {
            Logger.error("Error decrypting the command: " + e.getMessage());
        }
        return decryptedMessage;
    }

    private String encryptCommand(String command) {
        String encryptedMessage = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, socketPublicKey);

            byte[] encryptedBytes = cipher.doFinal(command.getBytes());

            encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes);

            //Logger.info("Encrypted Message: " + encryptedMessage);

            return encryptedMessage;
        } catch (Exception e) {
            Logger.error("Error encrypting the command: " + e.getMessage());
            return encryptedMessage;
        }
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
}
