package no.ntnu.gui.greenhouse;

import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.listeners.greenhouse.NodeStateListener;

// Girts comment: check spelling errors ;)
public class ButtonActionHanlder {

    static GreenhouseSimulator simulator;

    public ButtonActionHanlder(GreenhouseSimulator simulator) {
        this.simulator = simulator;
    }

    public static void handleAddNodeAction(NodeStateListener nodeStateListener) {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator, nodeStateListener);
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
    public static void handleStateOfSpecificActuator() {
        simulator.nodes.get(1).setActuator(2, true);
        System.out.println(simulator.nodes.get(1));
    }

//    public static void handleStateOfSpecificActuator(int node, int actuatorId, boolean state){
//        simulator.nodes.get(node).setActuator(actuatorId, state);
//        System.out.println(simulator.nodes.get(1));
//    }
}