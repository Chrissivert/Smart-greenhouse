package no.ntnu.controlpanel;

import no.ntnu.greenhouse.GreenhouseSimulator;

public class AddNodeActionHandler {

    public AddNodeActionHandler() {

    }

    public static void handleAddNodeAction() {
        AddNodeWindow inputWindow = new AddNodeWindow();
        inputWindow.showAndWait();
        System.out.println("Button clicked: Add node functionality");
    }
}