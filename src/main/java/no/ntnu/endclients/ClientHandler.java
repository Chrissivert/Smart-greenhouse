package no.ntnu.endclients;

import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.tools.Logger;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket socket;
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
                System.out.println("Client on port " + socket.getPort() + " sent message: " + inputLine);
                handleInput(inputLine);
//                server.broadcastMessage("hello clients");
                writer.println(inputLine);
            }
        } catch (IOException e) {
            Logger.error(" while reading from the socket: " + e.getMessage());
            e.printStackTrace();
        }
        String clientAddress = socket.getRemoteSocketAddress().toString();
        Logger.info("Client at " + clientAddress + " has disconnected.");
        simulator.removeDisconnectedClient(this);
    }

    /**
     * Sends a message to the connected client.
     *
     * @param message The message to be sent
     */
    public void sendMessage(String message) {
        System.out.println("Sending message: " + message);
        writer.println(message);
        System.out.println("Message sent to client on port " + socket.getPort());
    }


    /**
     * Handles the processing of a raw command, taking appropriate actions based on the command's content.
     *
     * @param rawCommand The command as a string
     */
    private void handleInput(String rawCommand) {
        if (rawCommand.equals("getNodes")) {
            handleGetNodesCommand();
        } if (rawCommand.equals("updateSensor")){
            handleUpdateSensorCommand();
        } else {
            processActuatorCommand(rawCommand);
        }
    }


    private void handleGetNodesCommand() {
       writer.println(simulator.getNodes());
    }


    private void handleUpdateSensorCommand() {
        writer.println(simulator.updateSensors());
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
            writer.println("  >>> Server response: Actuator[" + actuatorId +
                    "] on node " + nodeId + " is set to " + state);
        } else {
            Logger.error("Incorrect command format: " + rawCommand);
        }
    }
}
