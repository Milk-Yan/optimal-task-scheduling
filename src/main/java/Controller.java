import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML
    private VBox taskChartContainer;
    private StackedBarChart stackedBarChart;
    private CategoryAxis xAxis;

    private int[] processorFinishTimes;
    private XYChart.Series lastTask;
    private XYChart.Series lastIdle;

    public void setUpArgs(int numProcessors) {
        processorFinishTimes = new int[numProcessors];
        List<String> processorList = new ArrayList<>();
        for (int i = 0; i < numProcessors; i++) {
            processorList.add("Processor " + (i + 1));
        }
        xAxis.setCategories(FXCollections.<String>observableArrayList(processorList));
    }

    public void addTask(int processor, int duration, int startTime) {
        int idleTime = startTime - processorFinishTimes[processor];
        processorFinishTimes[processor] += idleTime + duration;

        lastIdle= new XYChart.Series();
        lastIdle.getData().add(new XYChart.Data("Processor " + (processor + 1), idleTime));

        lastTask = new XYChart.Series();
        lastTask.getData().add(new XYChart.Data("Processor " + (processor + 1), duration));

        stackedBarChart.getData().add(lastIdle);
        stackedBarChart.getData().add(lastTask);
    }

    public void removeLast() {
        stackedBarChart.getData().remove(lastTask);
        stackedBarChart.getData().remove(lastIdle);
    }

    @FXML
    private void initialize() {
        xAxis = new CategoryAxis();
        xAxis.setLabel("Processors");

        NumberAxis yAxis = new NumberAxis();

        stackedBarChart = new StackedBarChart(xAxis, yAxis);
        stackedBarChart.setLegendVisible(false);

        taskChartContainer.getChildren().add(stackedBarChart);
    }
}
