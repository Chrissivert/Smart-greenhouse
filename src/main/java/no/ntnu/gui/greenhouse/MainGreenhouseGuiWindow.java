package no.ntnu.gui.greenhouse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import no.ntnu.listeners.greenhouse.NodeStateListener;

/**
 * The main GUI window for greenhouse simulator.
 */
public class MainGreenhouseGuiWindow extends Scene {
    public static final int WIDTH = 300;
    public static final int HEIGHT = 300;

    public MainGreenhouseGuiWindow(NodeStateListener nodeStateListener) {
        super(createMainContent(nodeStateListener), WIDTH, HEIGHT);
    }

    private static Parent createMainContent(NodeStateListener nodeStateListener) {
        VBox container = new VBox(createAddNodeButton(nodeStateListener), createTurnOnAllActuatorsButton(), createTurnOffAllActuatorsButton(), createTurnOffSpecificActuator(), createInfoLabel(), createMasterImage(), createCopyrightNotice());
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);
        container.setSpacing(5);

        // Wrap the VBox container in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        scrollPane.setMaxWidth(Double.MAX_VALUE);
        scrollPane.setMaxHeight(Double.MAX_VALUE);

        return scrollPane;
    }

    private static Button createAddNodeButton(NodeStateListener nodeStateListener) {
        Button addNodeButton = new Button("Add node");
        addNodeButton.setOnAction(e -> ButtonActionHanlder.handleAddNodeAction(nodeStateListener));
        return addNodeButton;
    }

    private static Button createTurnOnAllActuatorsButton(){
        Button turnOnAllActuatorsButton = new Button("Turn on all actuators");
        turnOnAllActuatorsButton.setOnAction(e -> ButtonActionHanlder.handleTurnOnAllActuators());
        return turnOnAllActuatorsButton;
    }

    private static Button createTurnOffAllActuatorsButton(){
        Button turnOnAllActuatorsButton = new Button("Turn off all actuators");
        turnOnAllActuatorsButton.setOnAction(e -> ButtonActionHanlder.handleTurnOffAllActuators());
        return turnOnAllActuatorsButton;
    }

    private static Button createTurnOffSpecificActuator(){
        Button turnOffSpecificActuatorButton = new Button("Turn off specific actuator");
        turnOffSpecificActuatorButton.setOnAction(e -> ButtonActionHanlder.handleStateOfSpecificActuator());
        return turnOffSpecificActuatorButton;
    }

    private static Label createInfoLabel() {
        Label l = new Label("Close this window to stop the whole simulation");
        l.setWrapText(true);
        l.setPadding(new Insets(0, 0, 10, 0));
        return l;
    }

    private static Node createCopyrightNotice() {
        Label l = new Label("Image generated with Picsart");
        l.setFont(Font.font(10));
        return l;
    }

    private static Node createMasterImage() {
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

}
