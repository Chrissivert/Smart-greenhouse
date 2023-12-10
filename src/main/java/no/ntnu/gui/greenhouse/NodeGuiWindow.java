package no.ntnu.gui.greenhouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import no.ntnu.greenhouse.*;
import no.ntnu.gui.common.ActuatorPane;
import no.ntnu.gui.common.SensorPane;
import no.ntnu.listeners.common.ActuatorListener;
import no.ntnu.listeners.greenhouse.SensorListener;

/**
 * Window with GUI for overview and control of one specific sensor/actuator node and temperature graph.
 */
public class NodeGuiWindow extends Stage implements SensorListener, ActuatorListener {
    private static final double VERTICAL_OFFSET = 50;
    private static final double HORIZONTAL_OFFSET = 150;
    private static final double WINDOW_WIDTH = 300;
    private static final double WINDOW_HEIGHT = 300;
    private final SensorActuatorNode node;

    private ActuatorPane actuatorPane;
    private SensorPane sensorPane;

    private LineChart<Number, Number> lineChart;
    private XYChart.Series<Number, Number> series;



    /**
     * Create a GUI window for a specific node.
     *
     * @param node The node which will be handled in this window
     */
    public NodeGuiWindow(SensorActuatorNode node) {
        this.node = node;
        Scene scene = new Scene(createContent(), WINDOW_WIDTH, WINDOW_HEIGHT);
        setScene(scene);
        setTitle("Node " + node.getId());
        initializeListeners(node);
        setPositionAndSize();
    }

    /**
     * Set the position of the newly created GUIWindows.
     */

    private void setPositionAndSize() {
        setX((node.getId() - 1) * HORIZONTAL_OFFSET);
        setY(node.getId() * VERTICAL_OFFSET);
        setMinWidth(WINDOW_HEIGHT);
        setMinHeight(WINDOW_WIDTH);
    }

    /**
     * Create a graph within the GUI window displaying the temperature data.
     *
     * @param contentVBox The vBox holding the actuator- and sensorpanes.
     */
    private void createGraphWithData(VBox contentVBox) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Temperature");


        xAxis.setLowerBound(0);
        xAxis.setUpperBound(30);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(15);
        yAxis.setUpperBound(40);

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Temperature Data");

        series = new XYChart.Series<>();
        series.setName("Temperature Values");
        lineChart.getData().add(series);
        series.getNode().setStyle("-fx-stroke: " + getRandomColor());

        contentVBox.getChildren().add(lineChart);
    }

    /**
     * Initialize listeners for the node.
     *
     * @param node The node which will be handled in this window
     */

    private void initializeListeners(SensorActuatorNode node) {
        setOnCloseRequest(windowEvent -> shutDownNode());
        node.addSensorListener(this);
        node.addActuatorListener(this);
    }


    /**
     * Shut down the SensorActuatorNode.
     */
    private void shutDownNode() {
        node.stop();
    }


    /**
     * Create the content of the actuator- and sensorpanes.
     *
     * @return The content of the GUI window.
     */

    private Parent createContent() {
        actuatorPane = new ActuatorPane(node.getActuators(), false);
        sensorPane = new SensorPane(node.getSensors());

        HBox actuatorIdBox = new HBox();
        actuatorIdBox.setSpacing(10);

        for (Actuator actuator : node.getActuators()) {
            Integer actuatorId = actuator.getId();
            String actuatorType = actuator.getType();
            String displayText = "ActuatorId " + actuatorId + " Type: " + actuatorType;
            Text actuatorIdText = new Text(displayText);
            actuatorIdBox.getChildren().add(actuatorIdText);
        }

        VBox actuatorContentVBox = new VBox(actuatorPane, actuatorIdBox);
        actuatorContentVBox.setSpacing(10);

        ScrollPane actuatorScrollPane = new ScrollPane(actuatorContentVBox);
        actuatorScrollPane.setFitToWidth(true);
        actuatorScrollPane.setFitToHeight(true);
        actuatorScrollPane.setPrefSize(300, 200);

        VBox sensorContentVBox = new VBox(sensorPane);
        sensorContentVBox.setSpacing(10);

        ScrollPane sensorScrollPane = new ScrollPane(sensorContentVBox);
        sensorScrollPane.setFitToWidth(true);
        sensorScrollPane.setFitToHeight(true);
        sensorScrollPane.setPrefSize(300, 200);

        VBox contentVBox = new VBox(sensorScrollPane, actuatorScrollPane);
        contentVBox.setSpacing(10);
        createGraphWithData(contentVBox);

        ScrollPane scrollPane = new ScrollPane(contentVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        VBox.setVgrow(contentVBox, Priority.ALWAYS);
        HBox.setHgrow(contentVBox, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);

        splitPane.getItems().add(scrollPane);

        VBox actuatorSensorVBox = new VBox(sensorScrollPane, actuatorScrollPane);
        actuatorSensorVBox.setSpacing(10);
        splitPane.getItems().add(actuatorSensorVBox);

        return splitPane;
    }


    /**
     * Update the sensorpane with the new actuator.
     *
     * @param sensors The sensors which has been updated
     */

    @Override
    public void sensorsUpdated(List<Sensor> sensors) {
        System.out.println(" Sensors updated");
        if (sensorPane != null) {
            sensorPane.update(sensors);
        }

        for (Sensor sensor : sensors) {
            if (sensor.getType().equals("temperature")) {
                updateGraphWithSensorData(sensor.getReading().getValue());
            }
        }
    }


    /**
     * Update the actuatorpane with a specific actuator.
     *
     * @param nodeId   The id of the node which has been updated
     * @param actuator The actuator which has been updated
     */

    @Override
    public void actuatorUpdated(int nodeId, Actuator actuator) {
        if (actuatorPane != null) {
            actuatorPane.update(actuator);
        }
    }

    /**
     * Update the graph with the new sensor data.
     *
     * @param value The value of the sensor (temperature reading)
     */

    private void updateGraphWithSensorData(double value) {
        int time = series.getData().size() + 1;
        Platform.runLater(() -> {
            series.getData().add(new XYChart.Data<>(time, value));
        });
    }

    /**
     * Generate a random color.
     *
     * @return A random color.
     */

    private String getRandomColor() {
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return String.format("#%02x%02x%02x", r, g, b);
    }

}

