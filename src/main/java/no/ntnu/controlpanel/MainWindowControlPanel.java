package no.ntnu.controlpanel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import no.ntnu.gui.greenhouse.ButtonActionHandler;

import static no.ntnu.gui.greenhouse.GreenhouseApplication.simulator;

public class MainWindowControlPanel extends Scene {

    public static final int WIDTH = 300;
    public static final int HEIGHT = 300;
    private ButtonActionHandler buttonActionHandler;

    public MainWindowControlPanel() {
        super(new VBox(), WIDTH, HEIGHT);
        buttonActionHandler = new ButtonActionHandler(simulator);
        VBox mainContent = createMainContent();
        setRoot(mainContent);
    }


    private VBox createMainContent() {
        VBox container = new VBox();
        container.getChildren().addAll(
                createAddNodeButton(),
                createTurnOnAllActuatorsButton(),
                createTurnOffAllActuatorsButton(),
                createChangeSpecificActuatorStateStage(),
                createGetStateOfSpecificActuator()
        );
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);
        container.setSpacing(5);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxWidth(Double.MAX_VALUE);
        scrollPane.setMaxHeight(Double.MAX_VALUE);

        return container;
    }
    private Button createAddNodeButton() {
        Button addNodeButton = new Button("Add node");
        addNodeButton.setOnAction(e -> buttonActionHandler.handleAddNodeAction());
        return addNodeButton;
    }

    private Button createChangeSpecificActuatorStateStage() {
        Button changeSpecificActuatorButton = new Button("Change state of specific actuator");
        changeSpecificActuatorButton.setOnAction(e -> buttonActionHandler.createSetActuatorStateStage());
        return changeSpecificActuatorButton;
    }

    private Button createGetStateOfSpecificActuator() {
        Button turnOnAllActuatorsButton = new Button("Get state of specific actuator");
        turnOnAllActuatorsButton.setOnAction(e -> buttonActionHandler.getStateOfActuatorStage());
        return turnOnAllActuatorsButton;
    }

    private Button createTurnOnAllActuatorsButton() {
        Button turnOnAllActuatorsButton = new Button("Turn on all actuators");
        turnOnAllActuatorsButton.setOnAction(e -> buttonActionHandler.handleTurnOnAllActuators());
        return turnOnAllActuatorsButton;
    }

    private Button createTurnOffAllActuatorsButton() {
        Button turnOffAllActuatorsButton = new Button("Turn off all actuators");
        turnOffAllActuatorsButton.setOnAction(e -> buttonActionHandler.handleTurnOffAllActuators());
        return turnOffAllActuatorsButton;
    }
}
