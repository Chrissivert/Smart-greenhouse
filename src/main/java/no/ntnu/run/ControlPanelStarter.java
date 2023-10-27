package no.ntnu.run;

import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.Client;
import no.ntnu.gui.controlpanel.ControlPanelApplication;
import no.ntnu.tools.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Starter class for the control panel.
 * Note: we could launch the Application class directly, but then we would have issues with the
 * debugger (JavaFX modules not found)
 */
public class ControlPanelStarter {
    private final boolean fake;
    private Socket socket;

    private List<String> spawnActions = new ArrayList<>();


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
    }

    private void start() {
        ControlPanelLogic logic = new ControlPanelLogic();
        CommunicationChannel channel = initiateCommunication(logic, fake);
        ControlPanelApplication.startApp(logic, channel);
    }

    private CommunicationChannel initiateCommunication(ControlPanelLogic logic, boolean fake) {
        CommunicationChannel channel;
        if (fake) {
            channel = null;
        } else {
            channel = initiateEmptySpawner(logic);
        }
        return channel;
    }

    private void createSocketCommunication() {
        try {
            socket = new Socket("localhost", 1234); // Use the correct server address
            System.out.println("Connected to server");
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private CommunicationChannel initiateEmptySpawner(ControlPanelLogic logic) {
        createSocketCommunication();
        Client spawner = new Client(logic);
        logic.setCommunicationChannel(spawner);
        spawnFakeStuff(spawner, logic);

        return new Client(logic);
    }

    private void spawnFakeStuff(Client a, ControlPanelLogic logic) {
        a.spawnNode("4;3_window", 2);
        a.spawnNode("1", 3);
        a.spawnNode("1", 4);
    }

    public void sendMessageToServer(String message) {
        try (OutputStream out = socket.getOutputStream()) {
            byte[] messageBytes = message.getBytes();
            out.write(messageBytes);
            out.flush();
            System.out.println("Sent message to server: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static void waitFiveSeconds() {
        try {
            Thread.sleep(5000); // Sleep for 5 seconds (5000 milliseconds)
        } catch (InterruptedException e) {
            // Handle any exceptions that may occur during sleep
            e.printStackTrace();
        }
    }

}


//spawner.spawnNode("4;3_window", 2);
//        spawner.spawnNode("1", 3);
//        spawner.spawnNode("1", 4);
//        spawner.advertiseSensorData("4;temperature=27.4 °C,temperature=26.8 °C,humidity=80 %", 4);
//        spawner.spawnNode("8;2_heater", 5);
//        spawner.advertiseActuatorState(4, 1, true, 5);
//        spawner.advertiseActuatorState(4,  1, false, 6);
//        spawner.advertiseActuatorState(4,  1, true, 7);
//        spawner.advertiseActuatorState(4,  2, true, 7);
//        spawner.advertiseActuatorState(4,  1, false, 8);
//        spawner.advertiseActuatorState(4,  2, false, 8);
//        spawner.advertiseActuatorState(4,  1, true, 9);
//        spawner.advertiseActuatorState(4,  2, true, 9);
//        spawner.advertiseSensorData("4;temperature=22.4 °C,temperature=26.0 °C,humidity=81 %", 9);
//        spawner.advertiseSensorData("1;humidity=80 %,humidity=82 %", 10);
//        spawner.advertiseRemovedNode(8, 11);
//        spawner.advertiseRemovedNode(8, 12);
//        spawner.advertiseSensorData("1;temperature=25.4 °C,temperature=27.0 °C,humidity=67 %", 13);
//        spawner.advertiseSensorData("4;temperature=25.4 °C,temperature=27.0 °C,humidity=82 %", 14);
//        spawner.advertiseSensorData("4;temperature=25.4 °C,temperature=27.0 °C,humidity=82 %", 16);
