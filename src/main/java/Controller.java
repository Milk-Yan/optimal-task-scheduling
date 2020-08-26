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
    private Label inputGraphLabel;
    @FXML
    private Label totalTasksLabel;
    @FXML
    private Label threadCountLabel;
    @FXML
    private Label taskScheduledLabel;
    @FXML
    private Label currentBestLabel;
    @FXML
    private Label stateCountLabel;
    @FXML
    private Label timerLabel;

    @FXML
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
        System.out.println(processorFinishTimes[processor].peek());

        XYChart.Series idle = new XYChart.Series();
        idle.getData().add(new XYChart.Data("Processor " + (processor + 1), idleTime));

        XYChart.Series task = new XYChart.Series();
        task.getData().add(new XYChart.Data("Processor " + (processor + 1), duration));

        stackedBarChart.getData().add(idle);
        stackedBarChart.getData().add(task);
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
        currentBestLabel.setText(String.valueOf(bestFinishTime));
    }

    @FXML
    private void initialize() {
        xAxis = (CategoryAxis) stackedBarChart.getXAxis();
        stackedBarChart.setLegendVisible(false);

        long startTime = System.currentTimeMillis();
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsedMillis = System.currentTimeMillis() - startTime;
                int milliseconds = (int) (elapsedMillis % 1000);
                int seconds = (int) ((elapsedMillis / 1000) % 60);
                int minutes = (int) ((elapsedMillis / 1000) / 60);

                String millisecondsString = milliseconds < 100 ? "0" + (milliseconds < 10 ? "0" + (milliseconds == 0 ? "0" : milliseconds) : milliseconds) : String.valueOf(milliseconds);
                String secondsString = (seconds < 10 ? "0" + seconds : seconds) + ".";
                String minutesString = minutes == 0 ? "" : (minutes < 10 ? "0" + minutes : minutes) + ":";

                timerLabel.setText(minutesString + secondsString + millisecondsString);
            }
        }.start();
    }
}
