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
    public void handleStateOfSpecificActuator(int node, int actuatorId, boolean state) {
        SensorActuatorNode sensorActuatorNode = simulator.nodes.get(node);
        if (sensorActuatorNode != null) {
            sensorActuatorNode.setActuator(actuatorId, state);
            System.out.println(simulator.nodes.get(1));
        }  //System.out.println("Node not found or is null");

    }

    public void handleCreateChangeSpecificActuatorStateStage() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.createAndShowStage();
    }
}