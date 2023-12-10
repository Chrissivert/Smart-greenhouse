package no.ntnu.endclients;

import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.tools.EncrypterDecrypter;
import no.ntnu.tools.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * Handles each connected Client in a separate thread.
 */

public class ClientHandler extends Thread {
    protected Socket socket;
    private final GreenhouseSimulator simulator;
    private BufferedReader reader;
    private PrintWriter writer;


    /**
     * Creates a new instance of the ClientHandler class.
     *
     * @param socket    The socket representing the client connection
     * @param simulator The greenhouse simulator associated with this client handler
     */
    public ClientHandler(Socket socket, GreenhouseSimulator simulator) {
        this.socket = socket;
        this.simulator = simulator;
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
        try {
            System.out.println("Client on port: " + socket.getPort() + " is connected");
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                String a = EncrypterDecrypter.decryptMessage(inputLine);
                handleInput(a);
            }
        } catch (IOException e) {
            Logger.error("while reading from the socket: " + e.getMessage());
            e.printStackTrace();
        }
        String clientAddress = socket.getRemoteSocketAddress().toString();
        Logger.info("Client at " + clientAddress + " has disconnected.");
        simulator.removeDisconnectedClient(this);
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
        if (rawCommand.equals("getNodes")) {
            handleGetNodesCommand();
        } else if (rawCommand.equals("updateSensor")) {
            handleUpdateSensorCommand();
        } else {
            processActuatorCommand(rawCommand);
        }
    }


    /**
     * Sends the list of nodes in the greenhouse to the client in an encrypted format.
     */
    private void handleGetNodesCommand() {
        writer.println(EncrypterDecrypter.encryptMessage(simulator.getNodes()));
    }


    /**
     * Sends updated sensor-values to the client in an encrypted format.
     */

    private void handleUpdateSensorCommand() {
        writer.println(EncrypterDecrypter.encryptMessage(simulator.updateSensors()));
    }


    /**
     * Processes an actuator command, and sends a response to the client in
     * an encrypted format.
     *
     * @param rawCommand The command as a string
     */

    private void processActuatorCommand(String rawCommand) {
        String[] parts = rawCommand.split(",");
        if (parts.length == 3) {
            int nodeId = Integer.parseInt(parts[0].trim());
            int actuatorId = Integer.parseInt(parts[1].trim());
            int on = Integer.parseInt(parts[2].trim());
            boolean isOn = (on != 0);

            simulator.handleActuator(actuatorId, nodeId, isOn);

            String state = isOn ? "OFF" : "ON";

            writer.println(EncrypterDecrypter.encryptMessage("  >>> Server response: Actuator[" + actuatorId +
                    "] on node " + nodeId + " is set to " + state));
        } else {
            Logger.error("Incorrect command format: " + rawCommand);
        }
    }

    public void updateNodes() {
        writer.println(EncrypterDecrypter.encryptMessage("updateNodes"));
    }
}
