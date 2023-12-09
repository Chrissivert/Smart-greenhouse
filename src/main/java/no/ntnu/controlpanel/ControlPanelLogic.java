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

import static no.ntnu.tools.Parser.parseDoubleOrError;
import static no.ntnu.tools.Parser.parseIntegerOrError;

/**
 * The central logic of a control panel node. It uses a communication channel to send commands
 * and receive events. It supports listeners who will be notified on changes (for example, a new
 * node is added to the network, or a new sensor reading is received).
 * Note: this class may look like unnecessary forwarding of events to the GUI. In real projects
 * (read: "big projects") this logic class may do some "real processing" - such as storing events
 * in a database, doing some checks, sending emails, notifications, etc. Such things should never
 * be placed inside a GUI class (JavaFX classes). Therefore, we use proper structure here, even
 * though you may have no real control-panel logic in your projects.
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

    @Override
    public void onNodeAdded(SensorActuatorNodeInfo nodeInfo) {
        listeners.forEach(listener -> listener.onNodeAdded(nodeInfo));
    }

    @Override
    public void onNodeRemoved(int nodeId) {
        listeners.forEach(listener -> listener.onNodeRemoved(nodeId));
    }

    @Override
    public void onSensorData(int nodeId, List<SensorReading> sensors) {
        listeners.forEach(listener -> listener.onSensorData(nodeId, sensors));
    }

    @Override
    public void onActuatorStateChanged(int nodeId, int actuatorId, boolean isOn) {
        listeners.forEach(listener -> listener.onActuatorStateChanged(nodeId, actuatorId, isOn));
    }

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

    @Override
    public void actuatorUpdated(int nodeId, Actuator actuator) {
        if (communicationChannel != null) {
            communicationChannel.sendActuatorChange(nodeId, actuator.getId(), actuator.isOn());
            System.out.println("Sending actuator change to server");
        }
        listeners.forEach(listener ->
                listener.onActuatorStateChanged(nodeId, actuator.getId(), actuator.isOn())
        );
    }

    @Override
    public void onCommunicationChannelClosed() {
        Logger.info("Communication closed, updating logic...");
        if (communicationChannelListener != null) {
            communicationChannelListener.onCommunicationChannelClosed();
        }
    }


    public CommunicationChannel getCommunicationChannel() {
        return communicationChannel;
    }

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

    private ActuatorCollection parseActuators(String actuatorSpecification, int info) {
        String[] parts = actuatorSpecification.split(" ");
        ActuatorCollection actuatorList = new ActuatorCollection();
        for (String part : parts) {
            actuatorList.add(parseActuatorInfo(part, info));
        }
        return actuatorList;
    }

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

    private List<SensorReading> parseSensors(String sensorInfo) {
        List<SensorReading> readings = new LinkedList<>();
        String[] readingInfo = sensorInfo.split(",");
        for (String reading : readingInfo) {
            readings.add(parseReading(reading));
        }
        return readings;
    }

    private SensorReading parseReading(String reading) {
        String[] assignmentParts = reading.split("=");
        if (assignmentParts.length != 2) {
            throw new IllegalArgumentException("Invalid sensor reading specified: " + reading);
        }
        String[] valueParts = assignmentParts[1].split(" ");
        if (valueParts.length != 2) {
            throw new IllegalArgumentException("Invalid sensor value/unit: " + reading);
        }
        String sensorType = assignmentParts[0];
        double value = parseDoubleOrError(valueParts[0], "Invalid sensor value: " + valueParts[0]);
        String unit = valueParts[1];
        return new SensorReading(sensorType, value, unit);
    }
}
