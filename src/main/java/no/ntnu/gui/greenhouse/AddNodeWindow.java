package no.ntnu.gui.greenhouse;

import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
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

import java.util.ArrayList;
import java.util.List;

public class AddNodeWindow extends Stage {
    private TextField temperatureField;
    private TextField humidityField;
    private TextField windowsField;
    private TextField fansField;
    private TextField heatersField;

    private static TextField actuatorId;

    private static ChoiceBox<String> trueFalseChoiceBox;
    private ChoiceBox<String> currentNodeChoiceBox;

    GreenhouseSimulator simulator;

    static ButtonActionHandler buttonActionHandler;


    public AddNodeWindow(GreenhouseSimulator simulator) {
        this.simulator = simulator;
        buttonActionHandler = new ButtonActionHandler(simulator);
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

    public List<String> getNodeNames() {
        List<String> nodeNames = new ArrayList<>();
        for (SensorActuatorNode node : simulator.nodes.values()) {
            System.out.println("value added" + node.getId());
            nodeNames.add(String.valueOf(node.getId())); // Assuming a method getName() returns the node name
        }
        return nodeNames;
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

    public void createAndShowStage() {
        VBox vBox = new VBox();
        actuatorId = createCustomTextField("Enter actuatorId", 3, 200);
        vBox.getChildren().addAll(createAmountOfNodesChoiceBox(), actuatorId, createTurnOnOffChoiceBox(), handleActuatorChange());

        Scene scene = new Scene(vBox, 300, 200);

        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.show();
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

    public HBox handleActuatorChange() {
        Button createNodeButton = new Button("Confirm");
        createNodeButton.setOnAction(e -> buttonActionHandler.handleStateOfSpecificActuator(getParsedNodeChoice(),getActuatorId(),getTrueOrFalse()));
        return new HBox(createNodeButton);
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
}

