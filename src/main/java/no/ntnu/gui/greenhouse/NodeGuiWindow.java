package no.ntnu.gui.greenhouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.Sensor;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.gui.common.ActuatorPane;
import no.ntnu.gui.common.SensorPane;
import no.ntnu.listeners.common.ActuatorListener;
import no.ntnu.listeners.greenhouse.SensorListener;

/**
 * Window with GUI for overview and control of one specific sensor/actuator node.
 */
public class NodeGuiWindow extends Stage implements SensorListener, ActuatorListener {
    private static final double VERTICAL_OFFSET = 50;
    private static final double HORIZONTAL_OFFSET = 150;
    private static final double WINDOW_WIDTH = 300;
    private static final double WINDOW_HEIGHT = 300;
    private final SensorActuatorNode node;

    private ActuatorPane actuatorPane;
    private SensorPane sensorPane;

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

    private void setPositionAndSize() {
        setX((node.getId() - 1) * HORIZONTAL_OFFSET);
        setY(node.getId() * VERTICAL_OFFSET);
        setMinWidth(WINDOW_HEIGHT);
        setMinHeight(WINDOW_WIDTH);
    }

    private void createGraphWithData(VBox contentVBox) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Sensor Value");

        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Sensor Data");

        // Generate fake sensor data for the graph
        List<Double> sensorData = generateFakeSensorData(10); // Generating 10 fake data points

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Sensor Values");

        // Add fake sensor data to the graph
        int time = 1;
        for (Double value : sensorData) {
            series.getData().add(new XYChart.Data<>(time++, value));
        }

        lineChart.getData().add(series);

        contentVBox.getChildren().add(lineChart); // Add the chart to the VBox
    }
    private List<Double> generateFakeSensorData(int dataSize) {
        List<Double> sensorData = new ArrayList<>();
        Random random = new Random();

        // Generating random sensor data values
        for (int i = 0; i < dataSize; i++) {
            double value = 18 + random.nextDouble() * 10; // Random value between 18 and 28
            sensorData.add(value);
        }

        return sensorData;
    }


    private void initializeListeners(SensorActuatorNode node) {
        setOnCloseRequest(windowEvent -> shutDownNode());
        node.addSensorListener(this);
        node.addActuatorListener(this);
    }

    private void shutDownNode() {
        node.stop();
    }


    private Parent createContent() {
        actuatorPane = new ActuatorPane(node.getActuators(), false);
        sensorPane = new SensorPane(node.getSensors());

        VBox contentVBox = new VBox(sensorPane, actuatorPane);
        contentVBox.setSpacing(10);

        // Add the graph to the content VBox
        createGraphWithData(contentVBox);

        for (Actuator actuator : node.getActuators()) {
            Integer actuatorId = actuator.getId();
            String actuatorType = actuator.getType();
            String displayText = "ActuatorId " + actuatorId + " Type: " + actuatorType;
            Text actuatorIdText = new Text(displayText);
            contentVBox.getChildren().add(actuatorIdText);
        }

        ScrollPane scrollPane = new ScrollPane(contentVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        return scrollPane;
    }

    @Override
    public void sensorsUpdated(List<Sensor> sensors) {
        if (sensorPane != null) {
            sensorPane.update(sensors);
        }
    }

    @Override
    public void actuatorUpdated(int nodeId, Actuator actuator) {
        if (actuatorPane != null) {
            actuatorPane.update(actuator);
        }
    }
}
