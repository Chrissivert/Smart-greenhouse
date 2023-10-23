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
    // Create a socket connection when the application starts
    if (!fake) {
      connectToServer();
    }

    ControlPanelLogic logic = new ControlPanelLogic();
    CommunicationChannel channel = initiateCommunication(logic, fake);
    ControlPanelApplication.startApp(logic, channel);

    // ... Your application logic ...

    // Close the socket when the application exits
    if (!fake) {
      stopCommunication();
    }

    Logger.info("Exiting the control panel application");
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

  private CommunicationChannel initiateSocketCommunication(ControlPanelLogic logic) {
    // TODO - here you initiate TCP/UDP socket communication
    // You communication class(es) may want to get reference to the logic and call necessary
    // logic methods when events happen (for example, when sensor data is received)
    return null;
  }

  private boolean connect() {
    boolean success = false;
    try {
      Socket socket = new Socket("ntnu.no",80);
      System.out.println("Connected to server");
        success = true;
    } catch (IOException e) {
      System.out.println("Failed to connect to server"+e.getMessage());
    }
      return success;
  }

  private void disconnect() {
    try {
      socket.close();
      System.out.println("Disconnected from server");
    } catch (IOException e) {
      System.out.println("Failed to disconnect from server"+e.getMessage());
    }
  }

  private void connectToServer() {
    try {
      socket = new Socket("ntnu.no", 80); // Replace with the actual server details
      Logger.info("Connected to the server");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private CommunicationChannel initiateFakeSpawner(ControlPanelLogic logic) {
    // Here we pretend that some events will be received with a given delay
    FakeCommunicationChannel spawner = new FakeCommunicationChannel(logic);
    logic.setCommunicationChannel(spawner);
    spawner.spawnNode("4;3_window", 2);
    spawner.spawnNode("1", 3);
    spawner.spawnNode("1", 4);
    spawner.advertiseSensorData("4;temperature=27.4 °C,temperature=26.8 °C,humidity=80 %", 4);
    spawner.spawnNode("8;2_heater", 5);
    spawner.advertiseActuatorState(4, 1, true, 5);
    spawner.advertiseActuatorState(4,  1, false, 6);
    spawner.advertiseActuatorState(4,  1, true, 7);
    spawner.advertiseActuatorState(4,  2, true, 7);
    spawner.advertiseActuatorState(4,  1, false, 8);
    spawner.advertiseActuatorState(4,  2, false, 8);
    spawner.advertiseActuatorState(4,  1, true, 9);
    spawner.advertiseActuatorState(4,  2, true, 9);
    spawner.advertiseSensorData("4;temperature=22.4 °C,temperature=26.0 °C,humidity=81 %", 9);
    spawner.advertiseSensorData("1;humidity=80 %,humidity=82 %", 10);
    spawner.advertiseRemovedNode(8, 11);
    spawner.advertiseRemovedNode(8, 12);
    spawner.advertiseSensorData("1;temperature=25.4 °C,temperature=27.0 °C,humidity=67 %", 13);
    spawner.advertiseSensorData("4;temperature=25.4 °C,temperature=27.0 °C,humidity=82 %", 14);
    spawner.advertiseSensorData("4;temperature=25.4 °C,temperature=27.0 °C,humidity=82 %", 16);
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

  private void run() {
    if(connect()) {
        disconnect();
    }
    System.out.println("Exiting the control panel application");

    // TODO - here you run the TCP/UDP socket communication
  }

  public void sendControlMessage(String message) {
    if (!fake && socket != null && socket.isConnected()) {
      try {
        OutputStream out = socket.getOutputStream();
        out.write(message.getBytes());
        out.flush();
        Logger.info("Sent message: " + message);
      } catch (IOException e) {
        e.printStackTrace(); // Handle errors while sending data
      }
    }
  }

}
