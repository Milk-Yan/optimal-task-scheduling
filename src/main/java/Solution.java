import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public abstract class Solution {
    private TaskGraph taskGraph;
    private int numProcessors;
    private int numTasks;

    // does not change
    private int[] nodePriorities;
    private ArrayList<Integer>[] equivalentNodesList;
    private int[] maxLengthToExitNode;

    private int[] bestStartTime; // bestStartTime[i] => start time of task i in best schedule found so far
    private int[] bestScheduledOn; // bestScheduledOn[i] => processor that task i is scheduled on, in best schedule
    private int bestFinishTime; // earliest finishing time of schedules we have searched
    HashSet<Integer> seenSchedules = new HashSet<>();

    public abstract Schedule run(TaskGraph taskGraph, int numProcessors, int upperBoundTime) throws IOException, ClassNotFoundException;
}
