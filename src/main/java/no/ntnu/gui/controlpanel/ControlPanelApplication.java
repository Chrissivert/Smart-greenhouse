package no.ntnu.gui.controlpanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.ControlPanelSocket;
import no.ntnu.controlpanel.SensorActuatorNodeInfo;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.gui.common.ActuatorPane;
import no.ntnu.gui.common.SensorPane;
import no.ntnu.listeners.common.CommunicationChannelListener;
import no.ntnu.listeners.controlpanel.GreenhouseEventListener;
import no.ntnu.tools.Logger;


/**
 * Run a control panel with a graphical user interface (GUI), with JavaFX.
 */
public class ControlPanelApplication extends Application implements GreenhouseEventListener,
        CommunicationChannelListener {
    private static ControlPanelLogic logic;
    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;
    private static ControlPanelSocket channel;
    private TabPane nodeTabPane;
    private Scene mainScene;
    private final Map<Integer, SensorPane> sensorPanes = new HashMap<>();
    private final Map<Integer, ActuatorPane> actuatorPanes = new HashMap<>();
    private final Map<Integer, SensorActuatorNodeInfo> nodeInfos = new HashMap<>();
    private final Map<Integer, Tab> nodeTabs = new HashMap<>();

    /**
     * Application entrypoint for the GUI of a control panel.
     * Note - this is a workaround to avoid problems with JavaFX not finding the modules!
     * We need to use another wrapper-class for the debugger to work.
     *
     * @param logic   The logic of the control panel node
     * @param channel Communication channel for sending control commands and receiving events
     */
    public static void startApp(ControlPanelLogic logic, ControlPanelSocket channel) {
        if (logic == null) {
            throw new IllegalArgumentException("Control panel logic can't be null");
        }
        ControlPanelApplication.logic = logic;
        ControlPanelApplication.channel = channel;
        Logger.info("Running control panel GUI...");
        launch();
    }

    /**
     * Start the GUI for the control panel application.
     *
     * @param stage The stage
     */
    @Override
    public void start(Stage stage) {
        if (channel == null) {
            throw new IllegalStateException(
                    "No communication channel. See the README on how to use fake event spawner!");
        }
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);
        stage.setTitle("Control panel");
        mainScene = new Scene(createEmptyContent(), WIDTH, HEIGHT);
        stage.setScene(mainScene);
        stage.show();
        logic.addListener(this);
        logic.setCommunicationChannelListener(this);
        setCommunicationChannel(channel);
        channel.open();
        if (!channel.isOpen()) {
            logic.onCommunicationChannelClosed();
        }
    }

    /**
     * Create empty content for the GUI.
     *
     * @return A label with the text "Waiting for node data..."
     */

    private Label createEmptyContent() {
        Label l = new Label("Waiting for node data...");
        l.setAlignment(Pos.CENTER);
        return l;
    }


    /**
     * Event handler for when a node is added
     */
    @Override
    public void onNodeAdded(SensorActuatorNodeInfo nodeInfo) {
        Platform.runLater(() -> addNodeTab(nodeInfo));
    }

    /**
     * Event handler for when a node is removed
     */
    @Override
    public void onNodeRemoved(int nodeId) {
        Tab nodeTab = nodeTabs.get(nodeId);
        if (nodeTab != null) {
            Platform.runLater(() -> {
                removeNodeTab(nodeId, nodeTab);
                forgetNodeInfo(nodeId);
                if (nodeInfos.isEmpty()) {
                    removeNodeTabPane();
                }
            });
            Logger.info("Node " + nodeId + " removed");
        } else {
            Logger.error("Can't remove node " + nodeId + ", there is no Tab for it");
        }
    }

    /**
     * Remove the node tab pane from the main scene.
     */

    private void removeNodeTabPane() {
        mainScene.setRoot(createEmptyContent());
        nodeTabPane = null;
    }

    /**
     * Event handler for when sensor data is received
     */
    @Override
    public void onSensorData(int nodeId, List<SensorReading> sensors) {
        Logger.info("Sensor data from node " + nodeId);
        SensorPane sensorPane = sensorPanes.get(nodeId);
        if (sensorPane != null) {
            sensorPane.update(sensors);
        } else {
            Logger.error("No sensor section for node " + nodeId);
        }
    }

    /**
     * Event handler for when an actuator is turned on or off
     */
    @Override
    public void onActuatorStateChanged(int nodeId, int actuatorId, boolean isOn) {
        String state = isOn ? "ON" : "off";
        Logger.info("actuator[" + actuatorId + "] on node " + nodeId + " is " + state);
        ActuatorPane actuatorPane = actuatorPanes.get(nodeId);
        if (actuatorPane != null) {
            Actuator actuator = getStoredActuator(nodeId, actuatorId);
            if (actuator != null) {
                if (isOn) {
                    actuator.turnOn();
                } else {
                    actuator.turnOff();
                }
                actuatorPane.update(actuator);
            } else {
                Logger.error(" actuator not found");
            }
        } else {
            Logger.error("No actuator section for node " + nodeId);
        }
    }

    /**
     * Event handler for when an actuator is turned on or off
     */
    @Override
    public void onActuatorStateWithoutNofify(int nodeId, int actuatorId, boolean isOn) {
        String state = isOn ? "ON" : "off";
        Logger.info("actuator[" + actuatorId + "] on node " + nodeId + " is " + state);
        ActuatorPane actuatorPane = actuatorPanes.get(nodeId);
        if (actuatorPane != null) {
            Actuator actuator = getStoredActuator(nodeId, actuatorId);
            if (actuator != null) {
                if (isOn) {
                    actuator.turnOnDoNotNotify();
                } else {
                    actuator.turnOffDoNotNotify();
                }
                actuatorPane.update(actuator);
            } else {
                Logger.error(" actuator not found");
            }
        } else {
            Logger.error("No actuator section for node " + nodeId);
        }
    }

    /**
     * Get the stored actuator.
     *
     * @param nodeId     The ID of the node
     * @param actuatorId The ID of the actuator
     * @return The actuator, or null if it is not found
     */

    private Actuator getStoredActuator(int nodeId, int actuatorId) {
        Actuator actuator = null;
        SensorActuatorNodeInfo nodeInfo = nodeInfos.get(nodeId);
        if (nodeInfo != null) {
            actuator = nodeInfo.getActuator(actuatorId);
        }
        return actuator;
    }

    /**
     * Forget the node info.
     *
     * @param nodeId The ID of the node
     */

    private void forgetNodeInfo(int nodeId) {
        sensorPanes.remove(nodeId);
        actuatorPanes.remove(nodeId);
        nodeInfos.remove(nodeId);
    }

    /**
     * Remove the node tab.
     *
     * @param nodeId  The ID of the node
     * @param nodeTab The node tab
     */

    private void removeNodeTab(int nodeId, Tab nodeTab) {
        nodeTab.getTabPane().getTabs().remove(nodeTab);
        nodeTabs.remove(nodeId);
    }

    /**
     * Add a node tab.
     *
     * @param nodeInfo The node info
     */

    private void addNodeTab(SensorActuatorNodeInfo nodeInfo) {
        if (nodeTabPane == null) {
            nodeTabPane = new TabPane();
            mainScene.setRoot(nodeTabPane);
        }
        Tab nodeTab = nodeTabs.get(nodeInfo.getId());
        if (nodeTab == null) {
            nodeInfos.put(nodeInfo.getId(), nodeInfo);
            nodeTabPane.getTabs().add(createNodeTab(nodeInfo));
        } else {
            Logger.info("Duplicate node spawned, ignore it");
        }
    }

    /**
     * Create a node tab.
     *
     * @param nodeInfo The node info
     * @return The node tab
     */

    private Tab createNodeTab(SensorActuatorNodeInfo nodeInfo) {
        Tab tab = new Tab("Node " + nodeInfo.getId());
        SensorPane sensorPane = createEmptySensorPane();
        sensorPanes.put(nodeInfo.getId(), sensorPane);
        ActuatorPane actuatorPane = new ActuatorPane(nodeInfo.getActuators(), true);
        actuatorPanes.put(nodeInfo.getId(), actuatorPane);
        tab.setContent(new VBox(sensorPane, actuatorPane));
        nodeTabs.put(nodeInfo.getId(), tab);
        return tab;
    }

    /**
     * Create an empty sensor pane.
     *
     * @return The sensor pane
     */

    private SensorPane createEmptySensorPane() {
        return new SensorPane();
    }

    /**
     * Event handler for when the communication channel is closed
     */
    @Override
    public void onCommunicationChannelClosed() {
        Logger.info("Communication closed, closing the GUI");
        Platform.runLater(Platform::exit);
    }

    /**
     * Set the communication channel.
     *
     * @param channel The communication channel
     */

    public static void setCommunicationChannel(ControlPanelSocket channel) {
        ControlPanelApplication.channel = channel;
    }
}
