package solution;

import data.Schedule;
import data.Task;
import data.TaskGraph;

import java.util.*;

/**
 * Solution abstract class,SolutionParallel and SolutionSequential extends this class and used its methods to communicate
 * with the GUI. Global variables and fields that do not change are kept in this class.
 */
public abstract class Solution {
    protected TaskGraph taskGraph; //Contains information about the graph such as the adjacency matrix's
    protected int numProcessors;
    protected int numTasks;
    private boolean isVisual = false; //Flag to be used to update the GUI or not

    protected int[] nodePriorities; // The priority of a node to be scheduled
    protected ArrayList<Integer>[] equivalentNodesList; // index i contains a list of equivalent nodes for node i.
    protected int[] maxLengthToExitNode; // B levels of each node/task

    protected volatile int[] bestStartTime; // bestStartTime[i] => start time of task i in best schedule found so far
    protected volatile int[] bestScheduledOn; // bestScheduledOn[i] => processor that task i is scheduled on, in best schedule
    protected volatile int bestFinishTime; // earliest finishing time of schedules we have searched
    protected volatile HashSet<HashSet<Integer>> seenSchedules = new HashSet<>();

    protected volatile long stateCount = 0; //amount of states that we have searched
    protected volatile boolean isDone;
    protected volatile List<Task>[] bestSchedule; //current best schedule
    protected volatile boolean bestChanged = false;

    /**
     * @param taskGraph a data structure containing vital information about the DAG.
     * @param numProcessors number of processors we have to schedule the tasks on.
     * @return Schedule. Schedule encapsulates the an optimal schedule and the information needed when writing to the dot
     * file.
     */
    public Solution(TaskGraph taskGraph, int numProcessors) {
        this.taskGraph = taskGraph;
        this.numProcessors = numProcessors;
    }

    /**
     * Main method which runs the algorithm and returns a scheduling of tasks.
     */
    public abstract Schedule run();

    public void setVisual() {
        this.isVisual = true;
    }

    protected synchronized void updateStateCount() {
        this.stateCount++;
    }

    protected synchronized void setDone() {
        isDone = true;
    }

    /**
     * When we have found a schedule that is better than the current one, then we use this method to update it.
     */
    protected void updateBestScheduleOnVisual() {
        if (!isVisual) return;  //If the visual is not enabled, we dont do the following computation
        bestSchedule = new List[numProcessors];
        for (int i = 0; i < numProcessors; i++) {
            bestSchedule[i] = new ArrayList<>();
        }

        for (int i = 0; i < numTasks; i++) {
            if (bestScheduledOn[i] != -1) {
                Task task = new Task(bestStartTime[i], taskGraph.getDuration(i), false);
                bestSchedule[bestScheduledOn[i]].add(task);
            }
        }
        bestChanged = true;
    }

    /**
     * Set initial schedule of the solution so that it can be accessed by a poller, if it is being visualised.
     * @param schedule the initial schedule
     */
    public void setInitialSchedule(Schedule schedule) {
        this.bestFinishTime = schedule.getFinishTime();
        if (!isVisual) return;  //If the visual is not enabled, we dont do the following computation

        this.stateCount = 5 * taskGraph.getNumberOfTasks();

        bestSchedule = new List[numProcessors];
        for (int i = 0; i < numProcessors; i++) {
            bestSchedule[i] = new ArrayList<>();
        }

        Task[] tasks = schedule.getTasks();
        for (Task task : tasks) {
            Task transformedTask = new Task(task.getStartTime(),
                    task.getFinishTime() - task.getStartTime(), false);
            bestSchedule[task.getProcessor()].add(transformedTask);
        }
        bestChanged = true;
    }
}
