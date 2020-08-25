import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;

import javax.xml.soap.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Controller {
    @FXML
    private Pane taskChartContainer;
    @FXML
    private Label stateCountLabel;
    @FXML
    private Label bestFinishTimeLabel;
    @FXML
    private Label timerLabel;

    private StackedBarChart stackedBarChart;
    private CategoryAxis xAxis;

    private Stack<Integer> lastProcessor;
    private Stack<Integer>[] processorFinishTimes;

    public void setUpArgs(int numProcessors) {
        processorFinishTimes = new Stack[numProcessors];
        for (int i = 0; i < numProcessors; i++) {
            processorFinishTimes[i] = new Stack<>();
            processorFinishTimes[i].push(0);
        }
        lastProcessor = new Stack<>();

        List<String> xAxisProcessors = new ArrayList<>();
        for (int i = 0; i < numProcessors; i++) {
            xAxisProcessors.add("Processor " + (i + 1));
        }
        xAxis.setCategories(FXCollections.<String>observableArrayList(xAxisProcessors));
    }

    public void addTask(int processor, int duration, int startTime) {
        lastProcessor.push(processor);

        int idleTime = startTime - processorFinishTimes[processor].peek();
        processorFinishTimes[processor].push(startTime + duration);

        XYChart.Series idle = new XYChart.Series();
        idle.getData().add(new XYChart.Data("Processor " + (processor + 1), idleTime));

        XYChart.Series task = new XYChart.Series();
        task.getData().add(new XYChart.Data("Processor " + (processor + 1), duration));

        stackedBarChart.getData().add(idle);
        stackedBarChart.getData().add(task);

//        Node idleNode = stackedBarChart.lookup(".default-color" + count + ".chart-bar");
//        System.out.println(count);
//        idleNode.setStyle("-fx-bar-fill: rgba(255, 255, 255, 0);");
//        count++;
//        Node taskNode = stackedBarChart.lookup(".default-color" + count + ".chart-bar");
//        taskNode.setStyle("-fx-bar-fill:  #e9c4bc");
//        count++;
    }

    public void removeLast() {
        stackedBarChart.getData().remove(stackedBarChart.getData().size()-1);
        stackedBarChart.getData().remove(stackedBarChart.getData().size()-1);
        processorFinishTimes[lastProcessor.pop()].pop();
    }

    public void incrementState() {
        stateCountLabel.setText(String.valueOf(Integer.parseInt(stateCountLabel.getText()) + 1));
    }

    public void setBestFinishTime(int bestFinishTime) {
        bestFinishTimeLabel.setText(String.valueOf(bestFinishTime));
    }

    @FXML
    private void initialize() {
        xAxis = new CategoryAxis();

        NumberAxis yAxis = new NumberAxis();

        stackedBarChart = new StackedBarChart(xAxis, yAxis);
        stackedBarChart.setLegendVisible(false);

        taskChartContainer.getChildren().add(stackedBarChart);

        long startTime = System.currentTimeMillis();
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsedMillis = System.currentTimeMillis() - startTime ;
                int seconds = (int) (elapsedMillis / 1000);
                int minutes = seconds / 60;

                String secondsString = seconds < 10 ? "0" + seconds : seconds + "";
                String minutesString = minutes < 10 ? "0" + minutes : minutes + "";

                timerLabel.setText(minutesString + ":" + secondsString);
            }
        }.start();
    }
}
