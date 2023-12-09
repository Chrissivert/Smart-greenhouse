package no.ntnu.gui.greenhouse;

import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.tools.Logger;

public class ButtonActionHandler {

    GreenhouseSimulator simulator;

    public ButtonActionHandler(GreenhouseSimulator simulator) {
        this.simulator = simulator;
    }

    public void handleAddNodeAction() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.showAndWait();
    }

    public void handleTurnOnAllActuators() {
        for (SensorActuatorNode node : GreenhouseSimulator.nodes.values()) {
            if (node != null) {
                node.setAllActuators(true);
            }
        }
    }
    public void handleTurnOffAllActuators() {
        for (SensorActuatorNode node : GreenhouseSimulator.nodes.values()) {
            if (node != null) {
                node.setAllActuators(false);
            }
        }
    }


    public void setStateOfActuator(int node, int actuatorId, boolean state) {
        SensorActuatorNode sensorActuatorNode = GreenhouseSimulator.nodes.get(node);
        if (sensorActuatorNode != null) {
            sensorActuatorNode.setActuator(actuatorId, state);
        }
    }

    public boolean getStateOfActuator(int node, int actuatorId) {
        SensorActuatorNode sensorActuatorNode = GreenhouseSimulator.nodes.get(node);
        if (GreenhouseSimulator.nodes.get(node).getActuators().get(actuatorId)==null){
            Logger.info("Actuator does not exist");
        }else{
            System.out.println(sensorActuatorNode.getActuators().get(actuatorId).isOn());
        }
        return sensorActuatorNode.getActuators().get(actuatorId).isOn();
    }


    public void createSetActuatorStateStage() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.createSetActuatorStage();
     }

    public void createSetActuatorByTypeStateStage() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.createTurnOnOffAllByType();
    }


    public void getStateOfActuatorStage() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.createGetActuatorStage();
    }
}