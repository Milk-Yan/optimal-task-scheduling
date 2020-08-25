import java.util.LinkedList;
import java.util.List;

public class Solution {
    private TaskGraph taskGraph;
    private int numProcessors;
    private int numTasks;

    private int[] maxLengthToExitNode;
    private int[] inDegrees; // inDegrees[i] => number of unscheduled parent tasks of task i
    private int[] startTimes; // startTimes[i] => start time of task i
    private int[] scheduledOn;  // scheduledOn[i] => the processor task i is scheduled on
    private int[] processorFinishTimes; // processorFinishTimes[i] => finishing time of the last task scheduled on processor i
    private int remainingDuration = 0; // total duration of remaining tasks to be scheduled (used for pruning)

    private int[] bestStartTime; // bestStartTime[i] => start time of task i in best schedule found so far
    private int[] bestScheduledOn; // bestScheduledOn[i] => processor that task i is scheduled on, in best schedule
    private int bestFinishTime; // earliest finishing time of schedules we have searched

    LinkedList<Integer> candidateTasks; // queue of tasks with no unprocessed dependencies

    /**
     * Creates an optimal scheduling of tasks on specified number of processors.
     * @param taskGraph Graph containing tasks as nodes and their dependencies as edges.
     * @param numProcessors Number of processors to schedule the tasks on.
     * @param upperBoundTime Upper bound of running time that the optimal solution should do at least as good as.
     * @return optimal schedule found by the run method.
     */
    public Schedule run(TaskGraph taskGraph, int numProcessors, int upperBoundTime) {
        initialize(taskGraph, numProcessors, upperBoundTime);
        recursiveSearch();

        return createOutput();
    }

    /**
     * Recursively try to schedule a task on a processor.
     * Uses DFS to try all possible schedules.
     */
    private void recursiveSearch() {
        // Base case is when queue is empty, i.e. all tasks scheduled.
        if (candidateTasks.size() == 0) {
            int finishTime = findMaxInArray(processorFinishTimes);

            //If schedule time is better, update bestFinishTime and best schedule
            if (finishTime < bestFinishTime) {
                bestFinishTime = finishTime;

                for (int i = 0; i < bestStartTime.length; i++) {
                    bestScheduledOn[i] = scheduledOn[i];
                    bestStartTime[i] = startTimes[i];
                }
            }
            return;
        }

        // Information we need about the current schedule
        // minimal remaining time IF all remaining tasks are evenly distributed amongst processors.
        int loadBalancedRemainingTime = (int)Math.ceil(remainingDuration/(double)numProcessors);

        int earliestProcessorFinishTime = Integer.MAX_VALUE;
        int latestProcessorFinishTime = 0;
        for (int l = 0; l < numProcessors; l++) {
            earliestProcessorFinishTime = Math.min(processorFinishTimes[l], earliestProcessorFinishTime);
            latestProcessorFinishTime = Math.max(processorFinishTimes[l], latestProcessorFinishTime);
        }


        // Iterate through tasks
        for (int i = 0; i < candidateTasks.size(); i++) {
            int candidateTask = candidateTasks.remove();

            // Exit conditions 1
            boolean loadBalancingConstraint = earliestProcessorFinishTime + loadBalancedRemainingTime >= bestFinishTime;
            boolean criticalPathConstraint = earliestProcessorFinishTime + maxLengthToExitNode[candidateTask] >= bestFinishTime;
            boolean latestFinishTimeConstraint = latestProcessorFinishTime >= bestFinishTime;
            if(loadBalancingConstraint || criticalPathConstraint || latestFinishTimeConstraint) {
                candidateTasks.add(candidateTask);
                continue;
            }

            // Update state (Location 1: Candidate Task)
            remainingDuration -= taskGraph.getDuration(candidateTask);
            List<Integer> candidateChildren = taskGraph.getChildrenList(candidateTask);
            for (Integer candidateChild : candidateChildren) {
                inDegrees[candidateChild]--;
                if (inDegrees[candidateChild] == 0) {
                    candidateTasks.add(candidateChild);
                }
            }

            // Calculate information we need about constraints due to communication costs
            int maxDataArrival = 0;
            int processorCausingMaxDataArrival = 0;
            int secondMaxDataArrival = 0;

            List<Integer> parents = taskGraph.getParentsList(candidateTask);
            for(int parent : parents){
                int dataArrival = startTimes[parent] + taskGraph.getDuration(parent) + taskGraph.getCommCost(parent, candidateTask);
                if(dataArrival >= maxDataArrival){
                    if(scheduledOn[parent] != processorCausingMaxDataArrival){
                        secondMaxDataArrival = maxDataArrival;
                    }
                    maxDataArrival = dataArrival;
                    processorCausingMaxDataArrival = scheduledOn[parent];

                } else if(dataArrival >= secondMaxDataArrival){
                    if(scheduledOn[parent] != processorCausingMaxDataArrival){
                        secondMaxDataArrival = dataArrival;
                    }
                }
            }


            // Iterate through processors
            boolean hasBeenScheduledAtStart = false;
            for (int candidateProcessor = 0; candidateProcessor < numProcessors; candidateProcessor++) {
                // Avoid processor isomorphism
                if (processorFinishTimes[candidateProcessor] == 0) {
                    if (hasBeenScheduledAtStart) {
                        // Skip duplicated search space
                        continue;
                    } else {
                        hasBeenScheduledAtStart = true;
                    }
                }

                // Find earliest time to schedule candidate task on candidate processor
                int earliestStartTimeOnCurrentProcessor = processorFinishTimes[candidateProcessor];
                if(processorCausingMaxDataArrival != candidateProcessor) {
                    earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, maxDataArrival);
                } else {
                    earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, secondMaxDataArrival);
                }

                // Exit conditions 2: tighter constraint now that we have selected the processor
                criticalPathConstraint = earliestStartTimeOnCurrentProcessor + maxLengthToExitNode[candidateTask] >= bestFinishTime;
                if(criticalPathConstraint) {
                    continue;
                }

                // Update state (Location 2: Processors)
                int prevFinishTime = processorFinishTimes[candidateProcessor];
                processorFinishTimes[candidateProcessor] = earliestStartTimeOnCurrentProcessor + taskGraph.getDuration(candidateTask);
                scheduledOn[candidateTask] = candidateProcessor;
                startTimes[candidateTask] = earliestStartTimeOnCurrentProcessor;

                recursiveSearch();

                // Backtrack state (Location 2: Processors)
                processorFinishTimes[candidateProcessor] = prevFinishTime;
            }

            // Backtrack state (Location 1: Candidate Task)
            for (Integer candidateChild : candidateChildren) {
                // revert changes made to children
                inDegrees[candidateChild]++;
                if (inDegrees[candidateChild] == 1) {
                    candidateTasks.removeLast();
                }
            }
            remainingDuration += taskGraph.getDuration(candidateTask);
            candidateTasks.add(candidateTask);
        }
    }

    /**
     * Helper method to initialize all the fields required for the solution.
     */
    private void initialize(TaskGraph taskGraph, int numProcessors, int upperBoundTime) {
        this.taskGraph = taskGraph;
        this.numProcessors = numProcessors;

        maxLengthToExitNode = PreProcessor.maxLengthToExitNode(taskGraph);
        bestFinishTime = upperBoundTime;
        numTasks = taskGraph.getNumberOfTasks();

        inDegrees = new int[numTasks];
        bestStartTime = new int[numTasks];
        bestScheduledOn = new int[numTasks];
        processorFinishTimes = new int[numProcessors];
        startTimes = new int[numTasks];
        scheduledOn = new int[numTasks];
        candidateTasks = new LinkedList<>();

        for (int i = 0; i < numTasks; i++) {
            // calculate remaining duration of tasks to be scheduled
            remainingDuration += taskGraph.getDuration(i);
            inDegrees[i] = taskGraph.getParentsList(i).size();
            if (inDegrees[i] == 0) {
                candidateTasks.add(i);
            }
        }
    }

    /**
     * Helper method to create the output Schedule.
     * @return Optimal Schedule.
     */
    private Schedule createOutput() {
        Task[] optimalSchedule = new Task[numTasks];
        for (int i = 0; i < numTasks; i++) {
            Task t = new Task(i, bestStartTime[i],
                    bestStartTime[i] + taskGraph.getDuration(i), bestScheduledOn[i]);
            optimalSchedule[i] = t;
        }

        return new Schedule(optimalSchedule, bestFinishTime);
    }

    /**
     * Find the maximum value integer in the array. Returns Integer.MIN_VALUE if array is empty.
     * @return maximum value.
     */
    private int findMaxInArray(int[] arr) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            max = Math.max(max, arr[i]);
        }

        return max;
    }
}