package no.ntnu.gui.greenhouse;

import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import no.ntnu.greenhouse.Sensor;
import no.ntnu.gui.factory.TextFieldFactory;
import no.ntnu.greenhouse.DeviceFactory;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;

public class AddNodeWindow extends Stage {
    private TextField temperatureField;
    private TextField humidityField;
    private TextField windowsField;
    private TextField fansField;
    private TextField heatersField;

    private static TextField nodeId;

    private static TextField actuatorId;

    private static ChoiceBox<String> trueFalseChoiceBox;
    GreenhouseSimulator simulator;


    public AddNodeWindow(GreenhouseSimulator simulator) {
        this.simulator = simulator;
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
        newNode.start();
        NodeGuiWindow nodeGuiWindow = new NodeGuiWindow(newNode);
        nodeGuiWindow.show();
        close();
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

    private static TextField createCustomTextField(String promptText, int maxLength, int preferredSize) {
        return TextFieldFactory.createTextFieldWithDefaults()
                .withMaxLength(maxLength)
                .setPreferedSize(preferredSize)
                .addNumericOnlyFilter()
                .withPromptText(promptText)
                .build();
    }

    public static void createAndShowStage() {
        VBox vBox = new VBox();
        nodeId = createCustomTextField("Enter nodeId", 3, 200);
        actuatorId = createCustomTextField("Enter actuatorId", 3, 200);
        vBox.getChildren().addAll(nodeId, actuatorId, createChoiceBox(), handleActuatorChange());

        Scene scene = new Scene(vBox, 300, 200);

        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.show();
    }

    public static ChoiceBox<String> createChoiceBox(){
        trueFalseChoiceBox = new ChoiceBox<>();
        trueFalseChoiceBox.getItems().addAll("Turn on", "Turn off");
        return trueFalseChoiceBox;
    }

    public static HBox handleActuatorChange() {
        Button createNodeButton = new Button("Confirm");
        createNodeButton.setOnAction(e -> ButtonActionHanlder.handleStateOfSpecificActuator(getNodeId(),getActuatorId(),getTrueOrFalse()));
        return new HBox(createNodeButton);
    }


//    private static Button confirmSpecificActuatorStateButton(){
//        Button confirmSpecificActuatorStateButton = new Button("Confirm");
//        confirmSpecificActuatorStateButton.setOnAction(e -> AddNodeWindow.handleActuatorChange());
//        return confirmSpecificActuatorStateButton;
//    }

    public static int getNodeId() {
        return Integer.parseInt(nodeId.getText());
    }

    public static int getActuatorId() {
        return Integer.parseInt(actuatorId.getText());
    }

    public static boolean getTrueOrFalse() {
        String selectedValue = (String) trueFalseChoiceBox.getValue();
        return selectedValue != null && selectedValue.equals("Turn on");
    }
}

