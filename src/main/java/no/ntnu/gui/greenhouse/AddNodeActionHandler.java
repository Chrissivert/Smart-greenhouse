package no.ntnu.gui.greenhouse;

import no.ntnu.greenhouse.GreenhouseSimulator;

public class AddNodeActionHandler {

    static GreenhouseSimulator simulator;

    public AddNodeActionHandler(GreenhouseSimulator simulator) {
        this.simulator = simulator;
    }

    public static void handleAddNodeAction() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.showAndWait();
        System.out.println("Button clicked: Add node functionality");
    }
}