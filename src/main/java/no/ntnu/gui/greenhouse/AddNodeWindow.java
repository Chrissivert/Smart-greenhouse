package no.ntnu.gui.greenhouse;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import no.ntnu.gui.factory.TextFieldFactory;
import no.ntnu.greenhouse.DeviceFactory;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;

import java.util.ArrayList;
import java.util.List;

public class AddNodeWindow extends Stage {
    private TextField temperatureField;
    private TextField humidityField;
    private TextField windowsField;
    private TextField fansField;
    private TextField heatersField;

    private GreenhouseApplication greenhouseApplication = new GreenhouseApplication();

    private static TextField actuatorId;

    private static ChoiceBox<String> trueFalseChoiceBox;
    private ChoiceBox<String> currentNodeChoiceBox;

    GreenhouseSimulator simulator;

    static ButtonActionHandler buttonActionHandler;


    public AddNodeWindow(GreenhouseSimulator simulator) {
        this.simulator = simulator;
        buttonActionHandler = new ButtonActionHandler(simulator);
        VBox root = new VBox();

        temperatureField = createCustomTextField("Amount of temperature sensors", 3, 200);
        humidityField = createCustomTextField("Amount of humidity sensors", 3, 200);
        windowsField = createCustomTextField("Amount of windows", 3, 200);
        fansField = createCustomTextField("Amount of fans", 3, 200);
        heatersField = createCustomTextField("Amount of heaters", 3, 200);

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


    private void checkIfFieldsAreValid() {

        if (temperatureField.getText().isEmpty() || humidityField.getText().isEmpty()
                || windowsField.getText().isEmpty() || fansField.getText().isEmpty() || heatersField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Empty Fields");
            alert.setContentText("Please fill in all fields or provide valid values.");
            alert.showAndWait();
        } else {
            createNewNode();
        }
    }

    private void createNewNode() {
        SensorActuatorNode newNode = DeviceFactory.createNode(getTemperature(), getHumidity(), getMyWindows(), getFans(), getHeaters());
        simulator.addNode(newNode);
        greenhouseApplication.onNodeReady(newNode);
        newNode.start();
        newNode.addStateListener(greenhouseApplication);
        close();
    }

    public List<String> getNodeNames() {
        List<String> nodeNames = new ArrayList<>();
        for (SensorActuatorNode node : simulator.nodes.values()) {
            System.out.println("value added" + node.getId());
            nodeNames.add(String.valueOf(node.getId()));
        }
        return nodeNames;
    }


    private HBox createNodeButtons() {
        HBox root = new HBox();
        Button createNodeButton = new Button("Create Node");
        createNodeButton.setOnAction(e -> checkIfFieldsAreValid());

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

    public ChoiceBox<String> createTurnOnOffChoiceBox(){
        trueFalseChoiceBox = new ChoiceBox<>();
        trueFalseChoiceBox.getItems().addAll("Turn on", "Turn off");
        return trueFalseChoiceBox;
    }

    public  ChoiceBox<String> createAmountOfNodesChoiceBox(){
        currentNodeChoiceBox = new ChoiceBox<>();
        currentNodeChoiceBox.getItems().addAll(getNodeNames());
        return currentNodeChoiceBox;
    }

    public Button handleActuatorChange() {
        Button createNodeButton = new Button("Confirm");
        createNodeButton.setOnAction(e -> buttonActionHandler.setStateOfActuator(getParsedNodeChoice(),getActuatorId(),getTrueOrFalse()));
        return createNodeButton;
    }

    public Button createButtonWithText(String text) {
        return new Button(text);
    }

    public int getParsedNodeChoice() {
        String selectedValue = currentNodeChoiceBox.getValue();
        if (selectedValue != null) {
            return Integer.parseInt(selectedValue);
        } else {
            System.out.println("Please choice a currently active node");
            return -1;
        }
    }

    public static int getActuatorId() {
        return Integer.parseInt(actuatorId.getText());
    }

    public static boolean getTrueOrFalse() {
        String selectedValue = trueFalseChoiceBox.getValue();
        return selectedValue != null && selectedValue.equals("Turn on");
    }

    public void createGetActuatorStage() {
        VBox vBox = new VBox();
        actuatorId = createCustomTextField("Enter actuatorId", 3, 200);
        Button getStateButton = createButtonWithText("Get State");

        Label actuatorStateLabel = new Label("Actuator State: ");
        vBox.getChildren().addAll(createAmountOfNodesChoiceBox(), actuatorId, getStateButton, actuatorStateLabel);

        Scene scene = new Scene(vBox, 300, 200);
        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.show();

        getStateButton.setOnAction(e -> {
            int selectedNode = getParsedNodeChoice();
            int selectedActuatorId = getActuatorId();
            boolean actuatorState = buttonActionHandler.getStateOfActuator(selectedNode, selectedActuatorId);

            String stateText = "ActuatorId " + selectedActuatorId + " in node " + selectedNode + " is " + (actuatorState ? "ON" : "OFF");
            actuatorStateLabel.setText(stateText);
        });
    }


    public void createSetActuatorStage() {
        VBox vBox = new VBox();
        actuatorId = createCustomTextField("Enter actuatorId", 3, 200);
        vBox.getChildren().addAll(createAmountOfNodesChoiceBox(), actuatorId, createTurnOnOffChoiceBox(), handleActuatorChange());

        Scene scene = new Scene(vBox, 300, 200);

        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.show();
    }
}

