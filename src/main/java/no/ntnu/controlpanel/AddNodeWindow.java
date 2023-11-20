package no.ntnu.controlpanel;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import no.ntnu.greenhouse.DeviceFactory;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.gui.greenhouse.GreenhouseApplication;
import no.ntnu.gui.greenhouse.NodeGuiWindow;

public class AddNodeWindow extends Stage {
    private TextField temperatureField;
    private TextField humidityField;
    private TextField windowsField;
    private TextField fansField;
    private TextField heatersField;
    private GreenhouseSimulator simulator;


    public AddNodeWindow(GreenhouseSimulator simulator) {
        this.simulator = simulator;
        VBox root = new VBox();
        temperatureField = new TextField();
        humidityField = new TextField();
        windowsField = new TextField();
        fansField = new TextField();
        heatersField = new TextField();

        root.getChildren().addAll(
                temperatureField, humidityField, windowsField, fansField, heatersField, createNodeButtons() /* Add other fields here */);

        Scene scene = new Scene(root, 300, 200);
        setScene(scene);
    }

    // Method to retrieve entered values
    public int getTemperature() {
        return Integer.parseInt(temperatureField.getText());
    }

    public int getHumidity() {
        return Integer.parseInt(humidityField.getText());
    }

    public int getMyWindows() {
        return Integer.parseInt(windowsField.getText());
    }

    public int getFans() {
        return Integer.parseInt(fansField.getText());
    }

    public int getHeaters() {
        return Integer.parseInt(heatersField.getText());
    }


    private void createNodeFromFields(GreenhouseSimulator simulator) {
        int temperature = getTemperature();
        int humidity = getHumidity();
        int windows = getMyWindows();
        int fans = getFans();
        int heaters = getHeaters();

        SensorActuatorNode newNode = DeviceFactory.createNode(temperature, humidity, windows, fans, heaters);
        NodeGuiWindow nodeGuiWindow = new NodeGuiWindow(newNode);
        nodeGuiWindow.show();
    }

    private HBox createNodeButtons() {
        HBox root = new HBox();
        Button createNodeButton = new Button("Create Node");
        createNodeButton.setOnAction(e -> createNodeFromFields(simulator));

        Button goBackButton = new Button("Go Back");
        goBackButton.setOnAction(e -> close()); // Close the window on "Go Back" button click

        root.getChildren().addAll(createNodeButton, goBackButton);
        return root;
    }
}

