package no.ntnu.gui.greenhouse;

import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;

public class ButtonActionHanlder {

    static GreenhouseSimulator simulator;

    public ButtonActionHanlder(GreenhouseSimulator simulator) {
        this.simulator = simulator;
    }

    public static void handleAddNodeAction() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.showAndWait();
    }

    public static void handleTurnOnAllActuators() {
        for (int i = 0; i < simulator.nodes.size(); i++) {
            if (!(simulator.nodes.get(i) == null)) {
                simulator.nodes.get(i).setAllActuators(true);
            }
        }
    }
    public static void handleTurnOffAllActuators() {
        for (int i = 0; i < simulator.nodes.size(); i++) {
            if (!(simulator.nodes.get(i) == null)) {
                simulator.nodes.get(i).setAllActuators(false);
            }
        }
    }
}