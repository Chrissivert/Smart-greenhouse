package no.ntnu.controlpanel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.ActuatorCollection;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.listeners.common.ActuatorListener;
import no.ntnu.listeners.common.CommunicationChannelListener;
import no.ntnu.listeners.controlpanel.GreenhouseEventListener;
import no.ntnu.tools.Logger;

import static no.ntnu.tools.Parser.parseIntegerOrError;

/**
 * The logic of a controlPanel. It uses a communication channel to send commands
 * and receive events.
 */
public class ControlPanelLogic implements GreenhouseEventListener, ActuatorListener,
        CommunicationChannelListener {
    private final List<GreenhouseEventListener> listeners = new LinkedList<>();

    private List<SensorActuatorNodeInfo> nodeInfoList = new ArrayList<>();

    private CommunicationChannel communicationChannel;
    private CommunicationChannelListener communicationChannelListener;

    /**
     * Set the channel over which control commands will be sent to sensor/actuator nodes.
     *
     * @param communicationChannel The communication channel, the event sender
     */
    public void setCommunicationChannel(CommunicationChannel communicationChannel) {
        this.communicationChannel = communicationChannel;
    }

    /**
     * Set listener which will get notified when communication channel is closed.
     *
     * @param listener The listener
     */
    public void setCommunicationChannelListener(CommunicationChannelListener listener) {
        this.communicationChannelListener = listener;
    }

    /**
     * Add an event listener.
     *
     * @param listener The listener who will be notified on all events
     */
    public void addListener(GreenhouseEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Event handler for when a sensor/actuator node is added to the network.
     *
     * @param nodeInfo Information about the added node
     */
    @Override
    public void onNodeAdded(SensorActuatorNodeInfo nodeInfo) {
        listeners.forEach(listener -> listener.onNodeAdded(nodeInfo));
    }

    /**
     * Event handler for when a sensor/actuator node is removed from the network.
     *
     * @param nodeId The ID of the removed node
     */
    @Override
    public void onNodeRemoved(int nodeId) {
        listeners.forEach(listener -> listener.onNodeRemoved(nodeId));
    }

    /**
     * Event handler for when sensor data is received from a node.
     *
     * @param nodeId  The ID of the node sending sensor data
     * @param sensors List of sensor readings
     */
    @Override
    public void onSensorData(int nodeId, List<SensorReading> sensors) {
        listeners.forEach(listener -> listener.onSensorData(nodeId, sensors));
    }

    /**
     * Event handler for when the state of an actuator changes.
     *
     * @param nodeId     The ID of the node where the actuator is located
     * @param actuatorId The ID of the actuator whose state changed
     * @param isOn       The new state of the actuator
     */
    @Override
    public void onActuatorStateChanged(int nodeId, int actuatorId, boolean isOn) {
        listeners.forEach(listener -> listener.onActuatorStateChanged(nodeId, actuatorId, isOn));
    }

    /**
     * Event handler for when the state of an actuator changes.
     *
     * @param nodeId     The ID of the node where the actuator is located
     * @param actuatorId The ID of the actuator whose state changed
     * @param isOn       The new state of the actuator
     */
    @Override
    public void onActuatorStateWithoutNofify(int nodeId, int actuatorId, boolean isOn) {
        listeners.forEach(listener -> listener.onActuatorStateWithoutNofify(nodeId, actuatorId, isOn));
    }

    /**
     * Turn on or off all actuators in all connected nodes.
     *
     * @param isOn True to turn on, false to turn off
     */
    public void actuatorTurnOnAllActuators(boolean isOn) {
        for (SensorActuatorNodeInfo nodeInfo : nodeInfoList) {
            ActuatorCollection actuatorList = nodeInfo.getActuators();
            int nodeId = nodeInfo.getId();
            for (Actuator actuator : actuatorList) {
                if (communicationChannel != null) {
                    communicationChannel.sendActuatorChange(nodeId, actuator.getId(), isOn);
                    System.out.println("Sending actuator change to server");
                }
                listeners.forEach(listener ->
                        listener.onActuatorStateChanged(nodeId, actuator.getId(), isOn)
                );
            }
        }
    }

    /**
     * Event handler for when an actuator is updated.
     *
     * @param nodeId   The ID of the node where the actuator is located
     * @param actuator The updated actuator
     */
    @Override
    public void actuatorUpdated(int nodeId, Actuator actuator) {
        if (communicationChannel != null) {
            communicationChannel.sendActuatorChange(nodeId, actuator.getId(), actuator.isOn());
            System.out.println("Sending actuator change to greenhouse");
        }
        listeners.forEach(listener ->
                listener.onActuatorStateChanged(nodeId, actuator.getId(), actuator.isOn())
        );
    }

    /**
     * Event handler for when the communication channel is closed.
     */
    @Override
    public void onCommunicationChannelClosed() {
        Logger.info("Communication closed, updating logic...");
        if (communicationChannelListener != null) {
            communicationChannelListener.onCommunicationChannelClosed();
        }
    }


    /**
     * Get the communication channel.
     *
     * @return The communication channel
     */
    public CommunicationChannel getCommunicationChannel() {
        return communicationChannel;
    }

    /**
     * Create a SensorActuatorNodeInfo object from a specification string.
     *
     * @param specification The specification string in the format "nodeId;actuatorId1_actuatorType1 actuatorId2_actuatorType2 ..."
     * @return The created SensorActuatorNodeInfo
     * @throws IllegalArgumentException If the specification is empty or incorrectly formatted
     */
    public SensorActuatorNodeInfo createSensorNodeInfoFrom(String specification) {
        if (specification == null || specification.isEmpty()) {
            throw new IllegalArgumentException("Node specification can't be empty");
        }
        String[] parts = specification.split(";");
        if (parts.length > 2) {
            throw new IllegalArgumentException("Incorrect specification format");
        }
        int nodeId = parseIntegerOrError(parts[0], "Invalid node ID:" + parts[0]);
        SensorActuatorNodeInfo info = new SensorActuatorNodeInfo(nodeId);
        if (parts.length == 2) {
            ActuatorCollection actuatorList = parseActuators(parts[1], info.getId());
            info.setActuatorList(actuatorList);
        }
        nodeInfoList.add(info);
        return info;
    }

    /**
     * Parse a list of actuators from a string.
     *
     * @param actuatorSpecification The specification string in the format "actuatorId1_actuatorType1 actuatorId2_actuatorType2 ..."
     * @param info                  The info object which will be passed to the actuators
     * @return The created ActuatorCollection
     * @throws IllegalArgumentException If the specification is empty or incorrectly formatted
     */

    private ActuatorCollection parseActuators(String actuatorSpecification, int info) {
        String[] parts = actuatorSpecification.split(" ");
        ActuatorCollection actuatorList = new ActuatorCollection();
        for (String part : parts) {
            actuatorList.add(parseActuatorInfo(part, info));
        }
        return actuatorList;
    }

    /**
     * Parse an actuator from a string.
     *
     * @param s    The specification string in the format "actuatorId_actuatorType"
     * @param info The info object which will be passed to the actuator
     * @return The created Actuator
     * @throws IllegalArgumentException If the specification is empty or incorrectly formatted
     */

    private Actuator parseActuatorInfo(String s, int info) {
        String[] actuatorInfo = s.split("_");
        if (actuatorInfo.length != 2) {
            throw new IllegalArgumentException("Invalid actuator info format: " + s);
        }
        int actuatorId = parseIntegerOrError(actuatorInfo[0],
                "Invalid actuator count: " + actuatorInfo[0]);
        String actuatorType = actuatorInfo[1];
        Actuator actuator = new Actuator(actuatorId, actuatorType, info);
        actuator.setListener(this);
        return actuator;
    }
}
