package no.ntnu.gui.greenhouse;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import no.ntnu.gui.factory.TextFieldFactory;
import no.ntnu.greenhouse.DeviceFactory;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.listeners.greenhouse.NodeStateListener;

public class AddNodeWindow extends Stage {
    private TextField temperatureField;
    private TextField humidityField;
    private TextField windowsField;
    private TextField fansField;
    private TextField heatersField;

    // Girts comment: you are not going to change the simulator, right?
    private final GreenhouseSimulator simulator;
    private final NodeStateListener nodeStateListener;


    public AddNodeWindow(GreenhouseSimulator simulator, NodeStateListener nodeStateListener) {
        this.simulator = simulator;
        this.nodeStateListener = nodeStateListener;
        VBox root = new VBox();

        temperatureField = createCustomTextField("Amount of temperature sensors", 1, 200);
        humidityField = createCustomTextField("Amount of humidity sensors", 1, 200);
        windowsField = createCustomTextField("Amount of windows", 1, 200);
        fansField = createCustomTextField("Amount of fans", 1, 200);
        heatersField = createCustomTextField("Amount of heaters", 1, 200);

        root.getChildren().addAll(
                temperatureField, humidityField, windowsField, fansField, heatersField, createNodeButtons());

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane,300, 200);
        setScene(scene);
    }

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
        SensorActuatorNode newNode = DeviceFactory.createNode(getTemperature(), getHumidity(), getMyWindows(), getFans(), getHeaters());
        simulator.addNode(newNode);
        newNode.addStateListener(nodeStateListener);
        newNode.start();
    }

    private HBox createNodeButtons() {
        HBox root = new HBox();
        Button createNodeButton = new Button("Create Node");
        createNodeButton.setOnAction(e -> createNodeFromFields(simulator));

        Button goBackButton = new Button("Go Back");
        goBackButton.setOnAction(e -> close());

        root.getChildren().addAll(createNodeButton, goBackButton);
        return root;
    }

    private TextField createCustomTextField(String promptText, int maxLength, int preferredSize) {
        return TextFieldFactory.createTextFieldWithDefaults()
                .withMaxLength(maxLength)
                .setPreferedSize(preferredSize)
                .addNumericOnlyFilter()
                .withPromptText(promptText)
                .build();
    }
}

