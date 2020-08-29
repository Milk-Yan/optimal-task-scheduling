package gui;

import data.Task;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import solution.VisualThread;
import java.util.*;

/**
 * gui.Controller class for the visualisation (GUI). It connects to the fxml file.
 * Used to manage and modify information presented to the user in the GUI.
 */
public class Controller {

    // These labels are updated with information received by an object of the class in the GUI
    @FXML
    private Label inputGraphLabel;  // shows the name of the input graph
    @FXML
    private Label totalTasksLabel;  // shows the number of tasks (nodes) in the input graph
    @FXML
    private Label threadCountLabel; // shows the number of threads used to compute the solution
    @FXML
    private Label currentBestLabel; // shows the finish time of the shortest computed schedule so far
    @FXML
    private Label stateCountLabel;  // shows the number of states searched so far
    @FXML
    private Label timerLabel;   // shows the elapsed time so far
    @FXML
    private Label statusLabel;  // shows whether the program is running or has finished

    @FXML
    private Button startButton;

    // stackedBarChart has bars equal to the number of processors, and each bar is used to
    // visualise tasks being added to the processor in the GUI
    @FXML
    private StackedBarChart<String, Number> stackedBarChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private int numProcessors;

    private VisualThread visualThread;
    private Timer poller;
    private Timer timer;

    /**
     * This method sets up initial values for labels in the GUI. It also sets up data structures used when adding
     * and removing tasks from the stackedBarChart.
     * @param visualThread thread on which the algorithm runs
     * @param numProcessors number of processors
     * @param inputGraphName name of the input file
     * @param numTasks number of tasks (nodes) in the input file
     * @param numThreads number of threads the solution is being run on
     */
    public void setUpArgs(VisualThread visualThread, int numProcessors, String inputGraphName, int numTasks, int numThreads) {
        this.visualThread = visualThread;
        inputGraphLabel.setText(inputGraphName);
        totalTasksLabel.setText(numTasks + "");
        threadCountLabel.setText(numThreads + "");
        this.numProcessors = numProcessors;

        // Add each processor to the xAxis for the stackedBarChart
        List<String> xAxisProcessors = new ArrayList<>();
        for (int i = 0; i < numProcessors; i++) {
            xAxisProcessors.add((i + 1) + "");
        }
        xAxis.setCategories(FXCollections.observableArrayList(xAxisProcessors));
    }

    @FXML
    private void start() {
        final long startTime = System.currentTimeMillis();

        startButton.setDisable(true);
        setStatusRunning();

        visualThread.start();
        poller = new Timer();
        poller.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long stateCount = visualThread.getStateCount();
                boolean isDone = visualThread.isDone();

                Platform.runLater(() -> {
                    updateStateCountLabel(stateCount);
                    if (visualThread.getBestChanged()) {
                        currentBestLabel.setText(visualThread.getCurrentBest() + "");
                        updateStackedBarChart(visualThread.getBestSchedule());
                    }
                    if (isDone) {
                        stop();
                    }
                });
            }
        }, 0, 100);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - startTime;
                // Calculate the milliseconds, seconds, and minutes passed since the start of the program.
                int milliseconds = (int) ( elapsedMillis % 1000);
                int seconds = (int) ((elapsedMillis / 1000) % 60);
                int minutes = (int) ((elapsedMillis / (1000 * 60)) % 60);
                int hours = (int) (elapsedMillis / (1000 * 60 * 60));
                Platform.runLater(() -> {
                    timerLabel.setText(String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, milliseconds / 10));
                });
            }
        }, 0, 10);

    }

    private void stop() {
        poller.cancel();
        timer.cancel();
        setStatusFinished();
    }

    private void updateStateCountLabel(long stateCount) {
        if (stateCount < 1000) {
            stateCountLabel.setText(stateCount + "");
        } else if (stateCount < 1000000) {
            stateCountLabel.setText(stateCount/1000 + "K");
        } else if (stateCount < 1000000000) {
            stateCountLabel.setText(stateCount/1000000 + "M");
        } else {
            stateCountLabel.setText(stateCount/1000000000 + "B");
        }
    }

    /**
     * This method add tasks to specific processors in the startBarChart on the GUI. When
     * there is a gap between the start time of the task and the current finish time of the
     * processor, this method creates idle time to be added to the stackedBarChart, and
     * makes it invisible, as the bar chart cannot have gaps.
     */
    public void updateStackedBarChart(List<Task>[] bestSchedule) {
        stackedBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (int i = 0; i < numProcessors; i++) {
            Collections.sort(bestSchedule[i]);
        }

        for (int i = 0; i < numProcessors; i++) {
            List<Task> processorList = bestSchedule[i];
            if (processorList.size() != 0 && processorList.get(0).getStartTime() != 0) {
                Task idleTask = new Task(0, processorList.get(0).getStartTime(), true);
                processorList.add(0, idleTask);
            }
            int j = 1;
            while (processorList.size() > j) {
                Task currTask = processorList.get(j);
                Task prevTask = processorList.get(j - 1);
                if (currTask.getStartTime() != prevTask.getStartTime() + prevTask.getDuration()) {
                    int idleStartTime = prevTask.getStartTime() + prevTask.getDuration();
                    int idleDuration = currTask.getStartTime() - idleStartTime;
                    Task idleTask = new Task(idleStartTime, idleDuration, true);
                    processorList.add(j, idleTask);
                    j++;
                }
                j++;
            }
        }

        for (int i = 0; i < numProcessors; i++) {
            for (int j = 0; j < bestSchedule[i].size(); j++) {
                Task task = bestSchedule[i].get(j);
                final XYChart.Data<String, Number> bar = new XYChart.Data<>((numProcessors - i) + "", task.getDuration());
                bar.nodeProperty().addListener((ov, oldNode, node) -> {
                    if (node != null) {
                        if (task.isIdle()) {
                            node.setStyle("-fx-bar-fill: transparent");
                        } else {
                            node.setStyle("-fx-bar-fill: #e9c4bc;-fx-border-color: #444;");
                        }
                    }
                });
                series.getData().add(bar);
            }
        }

        stackedBarChart.getData().addAll(series);
    }

    /**
     * This method stops the timer and changes the status of the visualisation
     * from RUNNING to FINISHED.
     */
    private void setStatusFinished() {
        statusLabel.setText("FINISHED");
        statusLabel.setStyle("-fx-text-fill: forestgreen");
    }

    private void setStatusRunning() {
        statusLabel.setText("RUNNING");
        statusLabel.setStyle("-fx-text-fill: #ff3116");
    }
}
