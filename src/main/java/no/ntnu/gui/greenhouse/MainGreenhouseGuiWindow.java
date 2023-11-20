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
import no.ntnu.controlpanel.AddNodeActionHandler;

/**
 * The main GUI window for greenhouse simulator.
 */
public class MainGreenhouseGuiWindow extends Scene {
    public static final int WIDTH = 300;
    public static final int HEIGHT = 300;

    public MainGreenhouseGuiWindow() {
        super(createMainContent(), WIDTH, HEIGHT);
    }

    private static Parent createMainContent() {
        VBox container = new VBox(createAddNodeButton(), createInfoLabel(), createMasterImage(), createCopyrightNotice());
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);
        container.setSpacing(5);

        // Wrap the VBox container in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToHeight(true);// Allow horizontal scrolling if necessary
        scrollPane.setFitToWidth(true);

        scrollPane.setMaxWidth(Double.MAX_VALUE);
        scrollPane.setMaxHeight(Double.MAX_VALUE);

        return scrollPane;
    }

    private static Button createAddNodeButton() {
        Button addNodeButton = new Button("Add node");
        addNodeButton.setOnAction(e -> AddNodeActionHandler.handleAddNodeAction());
        return addNodeButton;
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
