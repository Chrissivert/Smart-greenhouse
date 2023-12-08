package no.ntnu.gui.greenhouse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.listeners.greenhouse.NodeStateListener;

public class MainGreenhouseGuiWindow extends Scene {
    public static final int WIDTH = 300;
    public static final int HEIGHT = 300;
    private GreenhouseSimulator simulator;
    private ButtonActionHandler buttonActionHandler;

    public MainGreenhouseGuiWindow(GreenhouseSimulator simulator) {
        super(new VBox(), WIDTH, HEIGHT);
        this.simulator = simulator;
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
                createGetStateOfSpecificActuator(),
                createInfoLabel(),
                createMasterImage(),
                createCopyrightNotice()
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

    private Label createInfoLabel() {
        Label infoLabel = new Label("Close this window to stop the whole simulation");
        infoLabel.setWrapText(true);
        infoLabel.setPadding(new Insets(0, 0, 10, 0));
        return infoLabel;
    }


    private Node createMasterImage() {
        Node node;
        try {
            InputStream fileContent = new FileInputStream("images/picsart_chuck.jpeg");
            ImageView imageView = new ImageView();
            imageView.setImage(new Image(fileContent));
            imageView.setFitWidth(100);
            imageView.setPreserveRatio(true);
            node = imageView;
        } catch (FileNotFoundException e) {
            node = new Label("Could not find image file: " + e.getMessage());
        }
        return node;
    }

    private Node createCopyrightNotice() {
        Label noticeLabel = new Label("Image generated with Picsart");
        noticeLabel.setFont(Font.font(10));
        return noticeLabel;
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