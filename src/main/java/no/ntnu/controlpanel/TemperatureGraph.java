package no.ntnu.controlpanel;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class TemperatureGraph extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Temperature over Time");

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Temperature");

        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Temperature Monitoring");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Temperature");

        series.getData().add(new XYChart.Data<>(1, 23.5));
        series.getData().add(new XYChart.Data<>(2, 24.0));
        series.getData().add(new XYChart.Data<>(3, 22.8));
        series.getData().add(new XYChart.Data<>(4, 23.2));
        series.getData().add(new XYChart.Data<>(5, 23.7));

        lineChart.getData().add(series);

        Scene scene = new Scene(lineChart, 800, 600);
        primaryStage.setScene(scene);

        primaryStage.show();
    }
}
