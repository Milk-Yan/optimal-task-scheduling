import java.io.IOException;
import java.util.*;

public abstract class Solution {
    protected TaskGraph taskGraph;
    protected int numProcessors;
    protected int numTasks;
    private boolean isVisual = false;

    protected int[] nodePriorities;
    protected ArrayList<Integer>[] equivalentNodesList;
    protected int[] maxLengthToExitNode;

    protected volatile int[] bestStartTime; // bestStartTime[i] => start time of task i in best schedule found so far
    protected volatile int[] bestScheduledOn; // bestScheduledOn[i] => processor that task i is scheduled on, in best schedule
    protected volatile int bestFinishTime; // earliest finishing time of schedules we have searched
    protected volatile HashSet<Integer> seenSchedules = new HashSet<>();

    protected volatile long stateCount;
    protected volatile boolean isDone;
    protected volatile List<Task>[] bestSchedule;
    protected volatile boolean bestChanged = false;

    public abstract Schedule run(TaskGraph taskGraph, int numProcessors, int upperBoundTime);

    public void setVisual() {
        this.isVisual = true;
    }

    protected synchronized void updateStateCount() {
        this.stateCount++;
    }

    protected synchronized void setDone() {
        isDone = true;
    }

    protected synchronized void updateBestSchedule() {
        if (!isVisual) return;
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
}
