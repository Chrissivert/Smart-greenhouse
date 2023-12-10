package no.ntnu.gui.greenhouse;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.gui.factory.TextFieldFactory;
import no.ntnu.greenhouse.DeviceFactory;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Create a window for adding a new node
 */
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

    /**
     * Create a window for adding a new node
     * @param simulator simulator to add the node to
     */
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

    /**
     * Get temperature from textfield
     * @return temperature
     */

    public int getTemperature() {
        return Integer.parseInt(temperatureField.getText());
    }

    /**
     * Get humidity from textfield
     * @return humidity
     */
    public int getHumidity() {
        return Integer.parseInt(humidityField.getText());
    }

    /**
     * Get amount of windows from textfield
     * @return amount of windows
     */
    public int getMyWindows() {
        return Integer.parseInt(windowsField.getText());
    }

    /**
     * Get amount of fans from textfield
     * @return amount of fans
     */
    public int getFans() {
        return Integer.parseInt(fansField.getText());
    }

    /**
     * Get amount of heaters from textfield
     * @return amount of heaters
     */
    public int getHeaters() {
        return Integer.parseInt(heatersField.getText());
    }


    /**
     * Check if all fields are valid
     */
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

    /**
     * Add newNode to the simulator and start it
     */

    private void createNewNode() {
        SensorActuatorNode newNode = DeviceFactory.createNode(getTemperature(), getHumidity(), getMyWindows(), getFans(), getHeaters());
        simulator.addNode(newNode);
        greenhouseApplication.onNodeReady(newNode);
        newNode.start();
        newNode.addStateListener(greenhouseApplication);
        close();
    }

    /**
     * Get all current nodes
     * @return list of all current nodes
     */

    public List<String> getNodeNames() {
        List<String> nodeNames = new ArrayList<>();
        for (SensorActuatorNode node : simulator.nodes.values()) {
            System.out.println("value added" + node.getId());
            nodeNames.add(String.valueOf(node.getId()));
        }
        return nodeNames;
    }

    /**
     * Create node buttons
     * @return Hbox with buttons
     */

    private HBox createNodeButtons() {
        HBox root = new HBox();
        Button createNodeButton = new Button("Create Node");
        createNodeButton.setOnAction(e -> checkIfFieldsAreValid());

        Button goBackButton = new Button("Go Back");
        goBackButton.setOnAction(e -> close());

        root.getChildren().addAll(createNodeButton, goBackButton);
        return root;
    }

    /**
     * Create custom textfield
     * @param promptText text to be displayed when textfield is empty
     * @param maxLength max length of textfield
     * @param preferredSize preferred size of textfield
     * @return custom textfield
     */

    private static TextField createCustomTextField(String promptText, int maxLength, int preferredSize) {
        return TextFieldFactory.createTextFieldWithDefaults()
                .withMaxLength(maxLength)
                .setPreferedSize(preferredSize)
                .addNumericOnlyFilter()
                .withPromptText(promptText)
                .build();
    }

    /**
     * Create choicebox with true or false
     * @return choicebox with true or false
     */

    public ChoiceBox<String> createTurnOnOffChoiceBox(){
        trueFalseChoiceBox = new ChoiceBox<>();
        trueFalseChoiceBox.getItems().addAll("Turn on", "Turn off");
        return trueFalseChoiceBox;
    }

    /**
     * Create choicebox with all current nodes
     * @return choicebox with all current nodes
     */

    public  ChoiceBox<String> createAmountOfNodesChoiceBox(){
        currentNodeChoiceBox = new ChoiceBox<>();
        currentNodeChoiceBox.getItems().addAll(getNodeNames());
        return currentNodeChoiceBox;
    }

    /**
     * Create and handle the action of changing the state of a specific actuator.
     * @return button
     */

    public Button handleActuatorChange() {
        Button createNodeButton = new Button("Confirm");
        createNodeButton.setOnAction(e -> buttonActionHandler.setStateOfActuator(getParsedNodeChoice(),getActuatorId(),getTrueOrFalse()));
        return createNodeButton;
    }

    /**
     * Create button with text
     * @param text text to be displayed on button
     * @return button with text
     */
    public Button createButtonWithText(String text) {
        return new Button(text);
    }


    /**
     * Get currently selected node from choicebox
     * @return currently selected node
     */
    public int getParsedNodeChoice() {
        String selectedValue = currentNodeChoiceBox.getValue();
        if (selectedValue != null) {
            return Integer.parseInt(selectedValue);
        } else {
            System.out.println("Please choice a currently active node");
            return -1;
        }
    }

    /**
     * Get actuator id from textfield
     * @return
     */
    public static int getActuatorId() {
        return Integer.parseInt(actuatorId.getText());
    }


    /**
     * Get true or false from choicebox
     * @return true or false
     */
    public static boolean getTrueOrFalse() {
        String selectedValue = trueFalseChoiceBox.getValue();
        return selectedValue != null && selectedValue.equals("Turn on");
    }


    /**
     * Create and show get state of actuator stage
     */

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

    /**
     * Create and show set state of actuator stage
     */

    public void createSetActuatorStage() {
        VBox vBox = new VBox();
        actuatorId = createCustomTextField("Enter actuatorId", 3, 200);
        vBox.getChildren().addAll(createAmountOfNodesChoiceBox(), actuatorId, createTurnOnOffChoiceBox(), handleActuatorChange());

        Scene scene = new Scene(vBox, 300, 200);

        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.show();
    }


    /**
     * Create and show set state of actuator by type stage
     */
    public void createTurnOnOffAllByTypeStage() {
        VBox vBox = new VBox();
        ChoiceBox<String> actuatorType = new ChoiceBox<>();
        actuatorType.getItems().addAll("Window", "Fan", "Heater");
        Button applyActionButton = new Button("Apply Action");
        applyActionButton.setOnAction(e -> {
            String selectedActuatorType = actuatorType.getValue();
            boolean isTurnOn = getTrueOrFalse();

            if (selectedActuatorType != null) {
                for (SensorActuatorNode node : simulator.nodes.values()) {
                    for (Actuator actuator : node.getActuators()) {
                        if (selectedActuatorType.equalsIgnoreCase("Window") && actuator.getType().equalsIgnoreCase("Window")) {
                            if (isTurnOn) {
                                actuator.turnOn();
                            } else {
                                actuator.turnOff();
                            }
                        } else if (selectedActuatorType.equalsIgnoreCase("Fan") && actuator.getType().equalsIgnoreCase("Fan")) {
                            if (isTurnOn) {
                                actuator.turnOn();
                            } else {
                                actuator.turnOff();
                            }
                        } else if (selectedActuatorType.equalsIgnoreCase("Heater") && actuator.getType().equalsIgnoreCase("Heater")) {
                            if (isTurnOn) {
                                actuator.turnOn();
                            } else {
                                actuator.turnOff();
                            }
                        }
                    }
                }
            } else {
                System.out.println("Please select an actuator type.");
            }
        });

        vBox.getChildren().addAll(actuatorType, createTurnOnOffChoiceBox(), applyActionButton);

        Scene scene = new Scene(vBox, 300, 200);
        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.show();
    }

}

