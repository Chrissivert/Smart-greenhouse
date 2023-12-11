package no.ntnu.run;

import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.ControlPanelSocket;
import no.ntnu.gui.controlpanel.ControlPanelApplication;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

/**
 * Starter class for the control panel.
 * Note: we could launch the Application class directly, but then we would have issues with the
 * debugger (JavaFX modules not found)
 */
public class ControlPanelStarter {

    public static final String SERVER_HOST = "localhost";
    private ControlPanelSocket socket;

    private ControlPanelLogic logic;

    public ControlPanelStarter() {
    }

    /**
     * Entrypoint for the application.
     *
     * @param args Command line arguments, only the first one of them used: when it is "fake",
     *             emulate fake events, when it is either something else or not present,
     *             use real socket communication.
     */
    public static void main(String[] args) {
        ControlPanelStarter starter = new ControlPanelStarter();
        starter.start();
    }

    /**
     * Starts the controlPanelApplication.
     */
    private void start() {
        this.logic = new ControlPanelLogic();
        ControlPanelSocket channel = initiateCommunication(logic);
        ControlPanelApplication.startApp(logic, channel);
        stopCommunication();
    }

    /**
     * Initiates the communication between the controlPanel and the server.
     *
     * @param logic The logic of the controlPanel.
     * @return The communicationChannel.
     */
    private ControlPanelSocket initiateCommunication(ControlPanelLogic logic) {
        ControlPanelSocket channel = initiateSocketCommunication(logic);
        initiateCommunicationThread();

        return channel;
    }

    /**
     * Initiates the listening loop for the server communications.
     */
    private void initiateCommunicationThread() {
        Thread serverThread = new Thread(this::initiateRealCommunication);
        serverThread.start();
    }

    /**
     * Starts the communication listening loop. Waits x amount of milliseconds before running runThread() again.
     * Change the period of waiting if the scale of the application is larger or smaller.
     */
    private void initiateRealCommunication() {

        while (socket == null || !socket.isOpen()) {
            try {
                sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                socket.runThread();
            }
        }, 0, 1);
        
    }

    /**
     * Gets the communicationChannel.
     *
     * @return The communicationChannel.
     */
    private CommunicationChannel getCommunicationChannel() {
        return this.logic.getCommunicationChannel();
    }

    /**
     * Initiates the socket communication of a connected controlPanel.
     *
     * @param logic The logic of the controlPanel.
     * @return The communicationChannel.
     */
    private ControlPanelSocket initiateSocketCommunication(ControlPanelLogic logic) {
        socket = new ControlPanelSocket(logic);
        logic.setCommunicationChannel(socket);
        return socket;
    }

    /**
     * Stops the communication of a connected controlPanel.
     */
    private void stopCommunication() {
        socket.close();
    }
}