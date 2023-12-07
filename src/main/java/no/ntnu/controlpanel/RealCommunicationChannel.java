package no.ntnu.controlpanel;

import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.DeviceFactory;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.tools.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import static no.ntnu.tools.Parser.parseIntegerOrError;

public class RealCommunicationChannel implements CommunicationChannel {


    private final ControlPanelLogic logic;
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public RealCommunicationChannel(ControlPanelLogic logic,String serverAddress, int port) throws IOException {
        this.logic = logic;
        this.socket = new Socket(serverAddress, port);
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }
    @Override
    public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn, String Type) {
        String state = isOn ? "ON" : "off";
        Logger.info("Sending command to greenhouse: turn " + state + " actuator"
                + "[" + actuatorId + "] on node " + nodeId);
    }

    private void parseActuators(String actuatorSpecification, SensorActuatorNodeInfo info) {
        String[] parts = actuatorSpecification.split(" ");
        for (String part : parts) {
            parseActuatorInfo(part, info);
        }
    }

    private void parseActuatorInfo(String s, SensorActuatorNodeInfo info) {
        String[] actuatorInfo = s.split("_");
        if (actuatorInfo.length != 2) {
            throw new IllegalArgumentException("Invalid actuator info format: " + s);
        }
        int actuatorCount = parseIntegerOrError(actuatorInfo[0],
                "Invalid actuator count: " + actuatorInfo[0]);
        String actuatorType = actuatorInfo[1];
        for (int i = 0; i < actuatorCount; ++i) {
            Actuator actuator = new Actuator(actuatorType, info.getId());
            actuator.setListener(logic);
            info.addActuator(actuator);
        }
    }

    private SensorActuatorNodeInfo createSensorNodeInfoFrom(String specification) {
        if (specification == null || specification.isEmpty()) {
            throw new IllegalArgumentException("Node specification can't be empty");
        }
        String[] parts = specification.split(";");
        if (parts.length > 3) {
            throw new IllegalArgumentException("Incorrect specification format");
        }
        int nodeId = parseIntegerOrError(parts[0], "Invalid node ID:" + parts[0]);
        SensorActuatorNodeInfo info = new SensorActuatorNodeInfo(nodeId);
        if (parts.length == 2) {
            parseActuators(parts[1], info);
        }
        return info;
    }

    /**
     * Spawn a new sensor/actuator node information after a given delay.
     *
     * @param specification A (temporary) manual configuration of the node in the following format
     *                      [nodeId] semicolon
     *                      [actuator_count_1] underscore [actuator_type_1] space ... space
     *                      [actuator_count_M] underscore [actuator_type_M]
     * @param delay         Delay in seconds
     */
    public void spawnNode(String specification, int delay) {
        SensorActuatorNodeInfo nodeInfo = createSensorNodeInfoFrom(specification);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logic.onNodeAdded(nodeInfo);
            }
        }, delay * 1000);

        System.out.println("node added");
    }

    @Override
    public boolean open() {
        return !socket.isClosed();
    }

    public boolean test(){
        return true;
    }

}
