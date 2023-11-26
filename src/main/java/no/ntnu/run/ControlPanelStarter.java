package no.ntnu.run;

import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.FakeCommunicationChannel;
import no.ntnu.gui.controlpanel.ControlPanelApplication;
import no.ntnu.tools.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Starter class for the control panel.
 * Note: we could launch the Application class directly, but then we would have issues with the
 * debugger (JavaFX modules not found)
 */
public class ControlPanelStarter {
    private final boolean fake;
    private Socket socket;


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
            channel = initiateFakeSpawner(logic);
        } else {
            channel = initiateSocketCommunication(logic);
        }
        return channel;
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



    private CommunicationChannel initiateSocketCommunication(ControlPanelLogic logic) {
        // TODO - here you initiate TCP/UDP socket communication
        try {
            Socket socket = new Socket("ntnu.no",80);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // You communication class(es) may want to get reference to the logic and call necessary
        // logic methods when events happen (for example, when sensor data is received)
        return null;
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
}