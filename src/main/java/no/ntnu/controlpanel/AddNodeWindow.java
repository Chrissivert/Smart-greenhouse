package no.ntnu.controlpanel;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import no.ntnu.greenhouse.DeviceFactory;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.gui.greenhouse.NodeGuiWindow;

public class AddNodeWindow extends Stage {
    private TextField temperatureField;
    private TextField humidityField;
    private TextField windowsField;
    private TextField fansField;
    private TextField heatersField;


    public AddNodeWindow() {
        VBox root = new VBox();

        temperatureField = createCustomTextField("Amount of temperature sensors", 1, 200);
        humidityField = createCustomTextField("Amount of humidity sensors", 1, 200);
        windowsField = createCustomTextField("Amount of windows", 1, 200);
        fansField = createCustomTextField("Amount of fans", 1, 200);
        heatersField = createCustomTextField("Amount of heaters", 1, 200);

        root.getChildren().addAll(
                temperatureField, humidityField, windowsField, fansField, heatersField, createNodeButtons() /* Add other fields here */);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane,300, 200);
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


    private void createNodeFromFields() {
        SensorActuatorNode newNode = DeviceFactory.createNode(getTemperature(), getHumidity(), getMyWindows(), getFans(), getHeaters());
        NodeGuiWindow nodeGuiWindow = new NodeGuiWindow(newNode);
        nodeGuiWindow.show();
    }

    private HBox createNodeButtons() {
        HBox root = new HBox();
        Button createNodeButton = new Button("Create Node");
        createNodeButton.setOnAction(e -> createNodeFromFields());

        Button goBackButton = new Button("Go Back");
        goBackButton.setOnAction(e -> close()); // Close the window on "Go Back" button click

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

