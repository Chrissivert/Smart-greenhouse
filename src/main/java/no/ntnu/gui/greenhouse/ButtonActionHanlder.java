package no.ntnu.gui.greenhouse;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.gui.factory.TextFieldFactory;

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
    public static void handleStateOfSpecificActuator(int node, int actuatorId, boolean state){
        simulator.nodes.get(node).setActuator(actuatorId, state);
        System.out.println(simulator.nodes.get(1));
    }
}