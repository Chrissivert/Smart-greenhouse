package no.ntnu.gui.greenhouse;

import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;

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
        for (int i = 0; i < simulator.nodes.size(); i++) {
            if (!(simulator.nodes.get(i) == null)) {
                simulator.nodes.get(i).setAllActuators(true);
            }
        }
    }
    public void handleTurnOffAllActuators() {
        for (int i = 0; i < simulator.nodes.size(); i++) {
            if (!(simulator.nodes.get(i) == null)) {
                simulator.nodes.get(i).setAllActuators(false);
            }
        }
    }
    public void setStateOfActuator(int node, int actuatorId, boolean state) {
        SensorActuatorNode sensorActuatorNode = simulator.nodes.get(node);
        if (sensorActuatorNode != null) {
            sensorActuatorNode.setActuator(actuatorId, state);
        }
    }

    public boolean getStateOfActuator(int node, int actuatorId) {
        SensorActuatorNode sensorActuatorNode = simulator.nodes.get(node);
        if (simulator.nodes.get(node).getActuators().get(actuatorId)==null){
            System.out.println("Actuator does not exist");
        }else{
            System.out.println(sensorActuatorNode.getActuators().get(actuatorId).isOn());
        }
        return sensorActuatorNode.getActuators().get(actuatorId).isOn();
    }


    public void createSetActuatorStateStage() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.createSetActuatorStage();
     }

    public void getStateOfActuatorStage() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.createGetActuatorStage();
    }
}