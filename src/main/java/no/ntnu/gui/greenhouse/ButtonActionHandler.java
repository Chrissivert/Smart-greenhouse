package no.ntnu.gui.greenhouse;

import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.tools.Logger;

/**
 * A class for handling button actions in the GUI.
 */

public class ButtonActionHandler {

    GreenhouseSimulator simulator;

    public ButtonActionHandler(GreenhouseSimulator simulator) {
        this.simulator = simulator;
    }

    /**
     * Handle the action of adding a new node.
     */

    public void handleAddNodeAction() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.showAndWait();
    }

    /**
     * Handle the action of turning on all actuators.
     */

    public void handleTurnOnAllActuators() {
        for (SensorActuatorNode node : GreenhouseSimulator.nodes.values()) {
            if (node != null) {
                this.simulator.handleAllActuators(node.getId(), true);
            }
        }
    }

    /**
     * Handle the action of turning off all actuators.
     */

    public void handleTurnOffAllActuators() {
        for (SensorActuatorNode node : GreenhouseSimulator.nodes.values()) {
            if (node != null) {
                this.simulator.handleAllActuators(node.getId(), false);
            }
        }
    }

    /**
     * Handle the action of changing the state of a specific actuator.
     */

    public void setStateOfActuator(int nodeId, int actuatorId, boolean state) {
        SensorActuatorNode sensorActuatorNode = GreenhouseSimulator.nodes.get(nodeId);
        if (sensorActuatorNode != null) {
            sensorActuatorNode.setActuator(actuatorId, state);
            simulator.broadcastActuatorStateChange(actuatorId,nodeId,!state);
        }
    }

    /**
     * Get the state of a specific actuator.
     *
     * @param node       The node which the actuator belongs to
     * @param actuatorId The id of the actuator
     * @return The state of the actuator
     */

    public boolean getStateOfActuator(int node, int actuatorId) {
        SensorActuatorNode sensorActuatorNode = GreenhouseSimulator.nodes.get(node);
        if (GreenhouseSimulator.nodes.get(node).getActuators().get(actuatorId) == null) {
            Logger.info("Actuator does not exist");
        } else {
            System.out.println(sensorActuatorNode.getActuators().get(actuatorId).isOn());
        }
        return sensorActuatorNode.getActuators().get(actuatorId).isOn();
    }


    /**
     * Handle the action of creating set state of actuator stage
     */

    public void createSetActuatorStateStage() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.createSetActuatorStage();
    }

    /**
     * Handle the action of creating set state of actuator by type stage
     */

    public void createSetActuatorByTypeStateStage() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.createTurnOnOffAllByTypeStage();
    }

    /**
     * Handle the action of creating get state of actuator stage
     */

    public void getStateOfActuatorStage() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.createGetActuatorStage();
    }

    /**
     * Handle the action of changing the state of all actuators of a specific type.
     * @param type actuator type
     * @param trueOrFalse actuator to be turned on or off
     */
    public void handleTurnOffActuatorsByType(String type, boolean trueOrFalse) {
        if (type != null) {
            GreenhouseSimulator.nodes.values().forEach(node -> node.getActuators().forEach(actuator -> {
                if (actuator.getType().equalsIgnoreCase(type)) {
                    actuator.set(trueOrFalse);
                    simulator.broadcastActuatorStateChange(actuator.getId(),node.getId(),!trueOrFalse);
                }
            }));
        }
    }
}