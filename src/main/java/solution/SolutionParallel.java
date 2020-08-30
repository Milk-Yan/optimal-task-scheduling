package solution;

import data.Schedule;
import data.Task;
import data.TaskGraph;
import solution.helpers.PartialScheduleHashGenerator;
import solution.helpers.PreProcessor;

import java.util.*;
import java.util.concurrent.*;

/**
 * The SolutionParallel class contains the code to find and return a optimal schedule for a given DAG. It makes uses of a
 * predefined amount of threads for its computation. It extends Solution with contains all the global variables and the
 * information about the given DAG.
 */
public class SolutionParallel extends Solution {
    private ForkJoinPool forkJoinPool;

    public void setNumCores(int numCores) {
        this.forkJoinPool = new ForkJoinPool(numCores);
    }

    /**
     * @param taskGraph      Graph containing tasks as nodes and their dependencies as edges.
     * @param numProcessors  Number of processors to schedule the tasks on.
     */
    public SolutionParallel (TaskGraph taskGraph, int numProcessors) {
        super(taskGraph, numProcessors);
    }

    /**
     * Creates an optimal scheduling of tasks on specified number of processors.
     * @return optimal schedule found by the run method.
     */
    public Schedule run() {
        initializeGlobalVars();
        SearchState initialSearchState = initializeState();

        RecursiveSearch recursiveSearch = new RecursiveSearch(initialSearchState);
        forkJoinPool.invoke(recursiveSearch);

        setDone();
        return createOutput();
    }

    /**
     * Inner RecursiveSearch class. This class represents an amount of work that must be done in the form of a state
     * that must be searched. A thread from the ForkJoinPool can execute the compute method which specifies the
     * work that it must do.
     */
    private class RecursiveSearch extends RecursiveAction {

        private SearchState searchState;

        private RecursiveSearch(SearchState searchState) {
            this.searchState = searchState;
        }

        /**
         * The compute is where a thread will start its work. This method searches a state, and recursively creates more
         * work/tasks/states to search.
         */
        @Override
        protected void compute() {
            updateStateCount();

            // Base case is when queue is empty, i.e. all tasks scheduled.
            if (searchState.candidateTasks.isEmpty()) {
                int finishTime = findMaxInArray(searchState.processorFinishTimes);

                synchronized (RecursiveSearch.class) {
                    //If schedule time is better, update bestFinishTime and best schedule
                    if (finishTime < bestFinishTime) {
                        bestFinishTime = finishTime;

                        for (int i = 0; i < bestStartTime.length; i++) {
                            bestScheduledOn[i] = searchState.scheduledOn[i];
                            bestStartTime[i] = searchState.taskStartTimes[i];
                        }
                        updateBestScheduleOnVisual();
                    }
                }
                return;
            }

            // Create a hash code for our partial schedule to check whether we have examined an equivalent schedule before
            // If we have seen an equivalent schedule we do not need to proceed
            int hashCode = PartialScheduleHashGenerator.generateHashCode(searchState.taskStartTimes, searchState.scheduledOn, numProcessors);
            synchronized (RecursiveSearch.class) {
                if (seenSchedules.contains(hashCode)) {
                    return;
                } else {
                    seenSchedules.add(hashCode);
                }
            }

            // Information we need about the current schedule
            // minimal remaining time IF all remaining tasks are evenly distributed amongst processors.
            int loadBalancedRemainingTime = (int) Math.ceil(searchState.remainingDuration / (double) numProcessors);

            int earliestProcessorFinishTime = Integer.MAX_VALUE;
            int latestProcessorFinishTime = 0;
            for (int l = 0; l < numProcessors; l++) {
                earliestProcessorFinishTime = Math.min(searchState.processorFinishTimes[l], earliestProcessorFinishTime);
                latestProcessorFinishTime = Math.max(searchState.processorFinishTimes[l], latestProcessorFinishTime);
            }

            int longestCriticalPath = 0;
            for (int task : searchState.candidateTasks) {
                int criticalPath = maxLengthToExitNode[task];
                if (criticalPath > longestCriticalPath) {
                    longestCriticalPath = criticalPath;
                }
            }

            // Iterate through tasks
            searchState.candidateTasks.sort(Comparator.comparingInt(a -> nodePriorities[a]));
            HashSet<Integer> seenTasks = new HashSet<>();
            for (int i = 0; i < searchState.candidateTasks.size(); i++) {
                List<RecursiveSearch> executableList = new ArrayList<>();

                int candidateTask = searchState.candidateTasks.remove();
                if (seenTasks.contains(candidateTask)) {
                    searchState.candidateTasks.add(candidateTask);
                    continue;
                } else {
                    ArrayList<Integer> equivalentNodes = equivalentNodesList[candidateTask];
                    seenTasks.addAll(equivalentNodes);
                }

                // Exit conditions 1
                boolean loadBalancingConstraint;
                boolean criticalPathConstraint;
                boolean latestFinishTimeConstraint;
                synchronized (RecursiveSearch.class) {
                    loadBalancingConstraint = earliestProcessorFinishTime + loadBalancedRemainingTime >= bestFinishTime;
                    criticalPathConstraint = earliestProcessorFinishTime + longestCriticalPath >= bestFinishTime;
                    latestFinishTimeConstraint = latestProcessorFinishTime >= bestFinishTime;
                }
                if (loadBalancingConstraint || criticalPathConstraint || latestFinishTimeConstraint) {
                    searchState.candidateTasks.add(candidateTask);
                    continue;
                }

                // Update state (Location 1: Candidate data.Task)
                searchState.remainingDuration -= taskGraph.getDuration(candidateTask);
                List<Integer> candidateChildren = taskGraph.getChildrenList(candidateTask);
                for (Integer candidateChild : candidateChildren) {
                    searchState.inDegrees[candidateChild]--;
                    if (searchState.inDegrees[candidateChild] == 0) {
                        searchState.candidateTasks.add(candidateChild);
                    }
                }

                // Calculate information we need about constraints due to communication costs
                int maxDataArrival = 0;
                int processorCausingMaxDataArrival = 0;
                int secondMaxDataArrival = 0;
                List<Integer> parents = taskGraph.getParentsList(candidateTask);
                for (int parent : parents) {
                    int dataArrival = searchState.taskStartTimes[parent] + taskGraph.getDuration(parent) + taskGraph.getCommCost(parent, candidateTask);
                    if (dataArrival >= maxDataArrival) {
                        if (searchState.scheduledOn[parent] != processorCausingMaxDataArrival) {
                            secondMaxDataArrival = maxDataArrival;
                        }
                        maxDataArrival = dataArrival;
                        processorCausingMaxDataArrival = searchState.scheduledOn[parent];

                    } else if (dataArrival >= secondMaxDataArrival) {
                        if (searchState.scheduledOn[parent] != processorCausingMaxDataArrival) {
                            secondMaxDataArrival = dataArrival;
                        }
                    }
                }

                boolean hasBeenScheduledAtStart = false;
                for (int candidateProcessor = 0; candidateProcessor < numProcessors; candidateProcessor++) { // Iterate through processors
                    // Avoid processor isomorphism
                    if (searchState.processorFinishTimes[candidateProcessor] == 0) {
                        if (hasBeenScheduledAtStart) {
                            // Skip duplicated search space
                            continue;
                        } else {
                            hasBeenScheduledAtStart = true;
                        }
                    }

                    // Find earliest time to schedule candidate task on candidate processor
                    int earliestStartTimeOnCurrentProcessor = searchState.processorFinishTimes[candidateProcessor];
                    if (processorCausingMaxDataArrival != candidateProcessor) {
                        earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, maxDataArrival);
                    } else {
                        earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, secondMaxDataArrival);
                    }

                    // Exit conditions 2: tighter constraint now that we have selected the processor
                    synchronized (RecursiveSearch.class) {
                        criticalPathConstraint = earliestStartTimeOnCurrentProcessor + maxLengthToExitNode[candidateTask] >= bestFinishTime;
                    }
                    if (criticalPathConstraint) {
                        continue;
                    }

                    // Update state (Location 2: Processors)
                    int prevFinishTime = searchState.processorFinishTimes[candidateProcessor];
                    searchState.processorFinishTimes[candidateProcessor] = earliestStartTimeOnCurrentProcessor + taskGraph.getDuration(candidateTask);
                    searchState.scheduledOn[candidateTask] = candidateProcessor;
                    searchState.taskStartTimes[candidateTask] = earliestStartTimeOnCurrentProcessor;

                    RecursiveSearch recursiveSearch;
                    recursiveSearch = new RecursiveSearch(searchState.getDeepCopy());
                    executableList.add(recursiveSearch);

                    // Backtrack state (Location 2: Processors)
                    searchState.processorFinishTimes[candidateProcessor] = prevFinishTime;
                }

                // Backtrack state (Location 1: Candidate data.Task)
                for (Integer candidateChild : candidateChildren) {
                    // revert changes made to children
                    searchState.inDegrees[candidateChild]++;
                    if (searchState.inDegrees[candidateChild] == 1) {
                        searchState.candidateTasks.removeLast();
                    }
                }
                searchState.remainingDuration += taskGraph.getDuration(candidateTask);
                searchState.candidateTasks.add(candidateTask);
                searchState.taskStartTimes[candidateTask] = -1;
                ForkJoinTask.invokeAll(executableList);
            }
        }

    }

    /**
     * Helper method to create the output data.Schedule.
     *
     * @return Optimal data.Schedule.
     */
    private Schedule createOutput() {
        Task[] optimalSchedule = new Task[numTasks];
        for (int i = 0; i < numTasks; i++) {
            Task t = new Task(bestStartTime[i],
                    bestStartTime[i] + taskGraph.getDuration(i), bestScheduledOn[i]);
            optimalSchedule[i] = t;
        }

        return new Schedule(optimalSchedule, bestFinishTime);
    }

    /**
     * Find the maximum value integer in the array. Returns Integer.MIN_VALUE if array is empty.
     *
     * @return maximum value.
     */
    private int findMaxInArray(int[] arr) {
        int max = Integer.MIN_VALUE;
        for (int j : arr) {
            max = Math.max(max, j);
        }

        return max;
    }

    /**
     * Helper method to initialize variables used by all threads.
     */
    private void initializeGlobalVars() {
        maxLengthToExitNode = PreProcessor.maxLengthToExitNode(taskGraph);
        nodePriorities = maxLengthToExitNode;
        numTasks = taskGraph.getNumberOfTasks();
        equivalentNodesList = PreProcessor.getNodeEquivalence(taskGraph);
        bestStartTime = new int[numTasks];
        bestScheduledOn = new int[numTasks];
    }

    /**
     * Helper method to create the initial state on which the algorithm runs.
     */
    private SearchState initializeState() {
        LinkedList<Integer> candidateTasks = new LinkedList<>();
        int[] inDegrees = new int[numTasks];
        int[] taskStartTimes = new int[numTasks];
        Arrays.fill(taskStartTimes, -1);
        int[] scheduledOn = new int[numTasks];
        int[] processorFinishTimes = new int[numProcessors];
        int remainingDuration = 0;
        for (int i = 0; i < numTasks; i++) {
            // calculate remaining duration of tasks to be scheduled
            remainingDuration += taskGraph.getDuration(i);
            inDegrees[i] = taskGraph.getParentsList(i).size();
            if (inDegrees[i] == 0) {
                candidateTasks.add(i);
            }
        }
        return new SearchState(candidateTasks, inDegrees, taskStartTimes,
                scheduledOn, processorFinishTimes, remainingDuration);
    }
}