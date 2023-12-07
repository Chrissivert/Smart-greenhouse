package no.ntnu.run;

import javafx.stage.Stage;
import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.FakeCommunicationChannel;
import no.ntnu.controlpanel.RealCommunicationChannel;
import no.ntnu.endclients.Server;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.gui.controlpanel.ControlPanelApplication;
import no.ntnu.gui.greenhouse.GreenhouseApplication;
import no.ntnu.tools.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static no.ntnu.gui.controlpanel.ControlPanelApplication.logic;

/**
 * Starter class for the control panel.
 * Note: we could launch the Application class directly, but then we would have issues with the
 * debugger (JavaFX modules not found)
 */
public class ControlPanelStarter implements CommunicationChannel {
    private final boolean fake;
    private Socket socket;

    private PrintWriter writer;
    private BufferedReader reader;


    public ControlPanelStarter(boolean fake) {
        this.fake = fake;
    }

    /**
     * Entrypoint for the application.
     *
     * @param args Command line arguments, only the first one of them used: when it is "fake",
     *             emulate fake events, when it is either something else or not present,
     *             use real socket communication.
     */
    public static void main(String[] args) {
        boolean fake = false;
        if (args.length == 1 && "fake".equals(args[0])) {
            fake = true;
            Logger.info("Using FAKE events");
        }
        ControlPanelStarter starter = new ControlPanelStarter(fake);
        starter.start();
        starter.listenForUserInput();
    }

    public void listenForUserInput() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter a command:");
            String userInput = scanner.nextLine();
            sendMessageToServer(userInput);

            System.out.println("Command to execute: " + userInput);

            if ("exit".equalsIgnoreCase(userInput.trim())) {
                break;
            }

            switch (userInput) {
                case "addNode":
                    addNodeCommand();
                    logic.onActuatorStateChanged(1, 2, true);
                    break;
                case "b":
                    logic.onActuatorStateChanged(4, 2, true);
                    break;

                case "advertiseSensor":
                    advertiseSensorCommand();
                    break;
                default:
                    System.out.println("Invalid command. Please enter a valid command.");
                    break;
            }
        }
        scanner.close();
    }


    private void addNodeCommand() {
        System.out.println(GreenhouseSimulator.nodes.size());
        FakeCommunicationChannel fakeCommunicationChannel = new FakeCommunicationChannel(logic);
        logic.setCommunicationChannel(fakeCommunicationChannel);
        fakeCommunicationChannel.spawnNode("4;3_window", 2);
    }

    private void advertiseSensorCommand() {
       // advertiseSensorData("5;temperature=25.5 °C,humidity=60 %", 3); // Advertise sensor data after a 3-second delay
    }

    private void start() {
        ControlPanelLogic logic = new ControlPanelLogic();
        CommunicationChannel channel = initiateCommunication(logic, fake);
        new Thread(() -> {
            ControlPanelApplication.startApp(logic, channel);
        }).start();
        startListening();
    }

    private CommunicationChannel initiateCommunication(ControlPanelLogic logic, boolean fake) {
        CommunicationChannel channel;
        if (fake) {
            channel = initiateFakeSpawner(logic);
        } else {
            channel = initiateSocketCommunication(logic);
        }
        return channel;
    }

    private CommunicationChannel getCommunicationChannel() {
        return logic.getCommunicationChannel();
    }

    public void sendMessageToServer(String message) {
        try {
            if (writer != null && socket != null && socket.isConnected()) {
                writer.println(message);
                writer.flush();
                System.out.println("Sent message to server: " + message);

                if (message.equals("stop")) {
                    stopCommunication();
                }
            } else {
                System.out.println("Socket or PrintWriter is not available or connected.");
            }
        } catch (Exception e) {
            System.err.println("Error sending message to server: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private CommunicationChannel initiateSocketCommunication(ControlPanelLogic logic) {
        RealCommunicationChannel spawner;
        try {
            spawner = new RealCommunicationChannel(logic,"localhost", 1234);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logic.setCommunicationChannel(spawner);
        //maybe get server to know of logic here
        try {
            socket = new Socket("localhost", 1234);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            sendMessageToServer("Initial message");
        } catch (IOException e) {
            System.err.println("Error establishing socket connection: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return spawner;
    }

    private CommunicationChannel initiateFakeSpawner(ControlPanelLogic logic) {
        FakeCommunicationChannel spawner = new FakeCommunicationChannel(logic);
        logic.setCommunicationChannel(spawner);
        spawner.spawnNode("4;3_window", 2);
        spawner.spawnNode("1", 3);
        spawner.spawnNode("1", 4);
        spawner.advertiseSensorData("4;temperature=27.4 °C,temperature=26.8 °C,humidity=80 %", 4);
        spawner.spawnNode("8;2_heater", 5);
        spawner.advertiseActuatorState(4, 1, true, 5);
        spawner.advertiseActuatorState(4,  1, false, 6);
        return spawner;
    }

    private void stopCommunication() {
        // Close the socket connection when done
        try {
            if (socket != null) {
                socket.close();
                Logger.info("Disconnected from the server");
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle any errors when closing the socket
        }
    }

    @Override
    public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn) {
        String state = isOn ? "ON" : "off";
        String message = "actuator " + state + " " + actuatorId + " " + nodeId;
        System.out.println(message);
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    if (socket != null && socket.isConnected()) {
                        String receivedMessage = reader.readLine();
                        if (receivedMessage != null) {
                            System.out.println("Received from server: " + receivedMessage);
                        }
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public boolean open() {
        return socket.isConnected();
    }
}