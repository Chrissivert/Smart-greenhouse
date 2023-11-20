package no.ntnu.controlpanel;

import no.ntnu.greenhouse.GreenhouseSimulator;

public class AddNodeActionHandler {
    private static GreenhouseSimulator simulator;

    public AddNodeActionHandler(GreenhouseSimulator simulator) {
        this.simulator = simulator;
    }

    public static void handleAddNodeAction() {
        AddNodeWindow inputWindow = new AddNodeWindow(simulator);
        inputWindow.showAndWait();
        System.out.println("Button clicked: Add node functionality");
    }
}