import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Controller class for the visualisation (GUI). It connects to the fxml file.
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
    private Label tasksScheduledLabel;   // shows the number of tasks scheduled in the current state
    @FXML
    private Label currentBestLabel; // shows the finish time of the shortest computed schedule so far
    @FXML
    private Label stateCountLabel;  // shows the number of states searched so far
    @FXML
    private Label timerLabel;   // shows the elapsed time so far
    @FXML
    private Label statusLabel;  // shows whether the program is running or has finished
    private boolean isRunning;  // true is program still computing optimal solution, false otherwise

    // stackedBarChart has bars equal to the number of processors, and each bar is used to
    // visualise tasks being added to the processor in the GUI
    @FXML
    private StackedBarChart<String, Number> stackedBarChart;
    private CategoryAxis xAxis;

    // these variables are used to keep track of the order at which tasks were added so that
    // we can backtrack and remove tasks in the correct order from the stackedBarChart in the GUI
    private Stack<Integer> lastProcessor;
    private Stack<Integer>[] processorFinishTimes;

    /**
     * This method initialises the stackedBarChart and timer for the GUI.
     */
    @FXML
    private void initialize() {
        xAxis = (CategoryAxis) stackedBarChart.getXAxis();
        stackedBarChart.setLegendVisible(false);

        // Get the current time, from which elapsed time will be calculated
        long startTime = System.currentTimeMillis();
        isRunning = true;

        // Start a timer
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsedMillis = System.currentTimeMillis() - startTime;

                // Calculate the milliseconds, seconds, and minutes passed since the start of the program.
                int milliseconds = (int) (elapsedMillis % 1000);
                int seconds = (int) ((elapsedMillis / 1000) % 60);
                int minutes = (int) ((elapsedMillis / (1000 * 60)) % 60);
                int hours = (int) (elapsedMillis / (1000 * 60 * 60));

                // Update the elapsed time if the program is still running
                if (isRunning) {
                    timerLabel.setText(String.format("%02d:%02d:%02d.%d", hours, minutes, seconds, milliseconds));
                }
            }
        }.start();
    }

    /**
     * This method sets up initial values for labels in the GUI. It also sets up data structures used when adding
     * and removing tasks from the stackedBarChart.
     * @param numProcessors number of processors
     * @param inputGraphName name of the input file
     * @param numTasks number of tasks (nodes) in the input file
     * @param numThreads number of threads the solution is being run on
     */
    public void setUpArgs(int numProcessors, String inputGraphName, int numTasks, int numThreads) {
        inputGraphLabel.setText(inputGraphName);
        totalTasksLabel.setText(numTasks + "");
        threadCountLabel.setText(numThreads + "");

        // initialise the finish time of all processors to 0
        processorFinishTimes = new Stack[numProcessors];
        for (int i = 0; i < numProcessors; i++) {
            processorFinishTimes[i] = new Stack<>();
            processorFinishTimes[i].push(0);
        }
        lastProcessor = new Stack<>();

        // Add each processor to the xAxis for the stackedBarChart
        List<String> xAxisProcessors = new ArrayList<>();
        for (int i = 0; i < numProcessors; i++) {
            xAxisProcessors.add("Processor " + (i + 1));
        }
        xAxis.setCategories(FXCollections.observableArrayList(xAxisProcessors));
    }

    /**
     * This method adds a task to a specific processor in the startBarChart on the GUI. When
     * there is a gap between the start time of the task and the current finish time of the
     * processor, this method creates idle time to be added to the stackedBarChart, and
     * makes it invisible, as the bar chart cannot have gaps.
     * @param processor the processor to add the task on
     * @param duration the duration the task runs for
     * @param startTime the start time of the task
     */
    public void addTask(int processor, int duration, int startTime) {
        // increment tasks scheduled on GUI
        tasksScheduledLabel.setText("" + (Integer.parseInt(tasksScheduledLabel.getText()) + 1));
        // this is the most recent processor a task has been added to
        lastProcessor.push(processor);

        // calculate the time difference between the task start time and processor finish time
        // update the finish time of the processor to match the finish time of the task
        int idleTime = startTime - processorFinishTimes[processor].peek();
        processorFinishTimes[processor].push(startTime + duration);

        // create data for the idle time to the correct processor on the stackedBarChart
        XYChart.Series<String, Number> idle = new XYChart.Series<>();
        idle.getData().add(new XYChart.Data<>("Processor " + (processor + 1), idleTime));

        // create data for the task itself to the correct processor on the stackedBarChart
        XYChart.Series<String, Number> task = new XYChart.Series<>();
        task.getData().add(new XYChart.Data<>("Processor " + (processor + 1), duration));

        //  add the idle and task data to the stackedBarChart
        stackedBarChart.getData().add(idle);
        stackedBarChart.getData().add(task);
    }

    /**
     * This algorithm removes the last task added from the stackedBarChart on the GUI
     */
    public void removeLast() {
        // decrement tasks scheduled on GUI
        tasksScheduledLabel.setText("" + (Integer.parseInt(tasksScheduledLabel.getText()) - 1));

        // remove twice because we remove the task and idle time
        stackedBarChart.getData().remove(stackedBarChart.getData().size()-1);
        stackedBarChart.getData().remove(stackedBarChart.getData().size()-1);

        // remove this processor from the last processor list, and also update its finish time
        processorFinishTimes[lastProcessor.pop()].pop();
    }

    /**
     * This method updates the number of states visited on the GUI
     */
    public void incrementState() {
        stateCountLabel.setText(String.valueOf(Integer.parseInt(stateCountLabel.getText()) + 1));
    }

    /**
     * This method updates the current finish time label on the GUI to the new best finish time.
     * @param bestFinishTime the finish time to update to
     */
    public void setBestFinishTime(int bestFinishTime) {
        currentBestLabel.setText(String.valueOf(bestFinishTime));
    }

    /**
     * This method stops the timer and changes the status of the visualisation
     * from RUNNING to FINISHED.
     */
    public void setStatusFinished() {
        statusLabel.setText("FINISHED");
        statusLabel.setStyle("-fx-text-fill: forestgreen");
        isRunning = false;
    }
}
