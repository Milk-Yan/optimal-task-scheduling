import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class SolutionParallel extends Solution {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool(3);

    private TaskGraph taskGraph;
    private int numProcessors;
    private int numTasks;

    // does not change
    private int[] nodePriorities;  // a nodes priority for scheduling
    private ArrayList<Integer>[] equivalentNodesList;  // a list of equivalent node for node i in index i
    private int[] maxLengthToExitNode; // a nodes bottom level

    private int[] bestStartTime; // bestStartTime[i] => start time of task i in best schedule found so far
    private volatile int[] bestScheduledOn; // bestScheduledOn[i] => processor that task i is scheduled on, in best schedule
    private volatile int bestFinishTime; // earliest finishing time of schedules we have searched
    private volatile HashSet<Integer> seenSchedules = new HashSet<>();

    /**
     * Creates an optimal scheduling of tasks on specified number of processors.
     *
     * @param taskGraph      Graph containing tasks as nodes and their dependencies as edges.
     * @param numProcessors  Number of processors to schedule the tasks on.
     * @param upperBoundTime Upper bound of running time that the optimal solution should do at least as good as.
     * @return optimal schedule found by the run method.
     */
    public Schedule run(TaskGraph taskGraph, int numProcessors, int upperBoundTime) throws IOException, ClassNotFoundException {
        initializeGlobalVars(taskGraph, numProcessors, upperBoundTime);
        State initialState = initializeState(taskGraph, numProcessors);

        RecursiveSearch recursiveSearch = new RecursiveSearch(initialState);
        forkJoinPool.invoke(recursiveSearch);

        return createOutput();
    }

    private class RecursiveSearch extends RecursiveAction {
        private State state;

        private RecursiveSearch(State state) {
            this.state = state;
        }

        /**
         * Recursively try to schedule a task on a processor.
         * Uses DFS to try all possible schedules.
         */
        @Override
        protected void compute() {
            System.out.println(Thread.currentThread().getName());
            // Base case is when queue is empty, i.e. all tasks scheduled.
            if (state.candidateTasks.isEmpty()) {
                int finishTime = findMaxInArray(state.processorFinishTimes);

                synchronized (this) {
                    //If schedule time is better, update bestFinishTime and best schedule
                    if (finishTime < bestFinishTime) {
                        bestFinishTime = finishTime;

                        for (int i = 0; i < bestStartTime.length; i++) {
                            bestScheduledOn[i] = state.scheduledOn[i];
                            bestStartTime[i] = state.taskStartTimes[i];
                        }
                    }
                }
                return;
            }

            // Create a hash code for our partial schedule to check whether we have examined an equivalent schedule before
            // If we have seen an equivalent schedule we do not need to proceed
            int hashCode = PartialSchedule.generateHashCode(state.taskStartTimes, state.scheduledOn, numProcessors);
            synchronized (this) {
                if (seenSchedules.contains(hashCode)) {
                    return;
                } else {
                    // Find if we can complete the tasks in Fixed Task Order (FTO)
                    LinkedList<Integer> ftoSorted = toFTOList(new LinkedList<>(state.candidateTasks));
                    if (ftoSorted != null) {
                        state.candidateTasks = ftoSorted;
                        try {
                            getFTOSchedule(ftoSorted);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    seenSchedules.add(hashCode);
                }
            }

            // Information we need about the current schedule
            // minimal remaining time IF all remaining tasks are evenly distributed amongst processors.
            int loadBalancedRemainingTime = (int) Math.ceil(state.remainingDuration / (double) numProcessors);

            int earliestProcessorFinishTime = Integer.MAX_VALUE;
            int latestProcessorFinishTime = 0;
            for (int l = 0; l < numProcessors; l++) {
                earliestProcessorFinishTime = Math.min(state.processorFinishTimes[l], earliestProcessorFinishTime);
                latestProcessorFinishTime = Math.max(state.processorFinishTimes[l], latestProcessorFinishTime);
            }

            int longestCriticalPath = 0;
            for (int task : state.candidateTasks) {
                int criticalPath = maxLengthToExitNode[task];
                if (criticalPath > longestCriticalPath) {
                    longestCriticalPath = criticalPath;
                }
            }

            // Iterate through tasks
            state.candidateTasks.sort(Comparator.comparingInt(a -> nodePriorities[a]));
            HashSet<Integer> seenTasks = new HashSet<>();
            for (int i = 0; i < state.candidateTasks.size(); i++) {
                List<RecursiveSearch> executableList = new ArrayList<>();

                int candidateTask = state.candidateTasks.remove();
                if (seenTasks.contains(candidateTask)) {
                    state.candidateTasks.add(candidateTask);
                    continue;
                } else {
                    ArrayList<Integer> equivalentNodes = equivalentNodesList[candidateTask];
                    seenTasks.addAll(equivalentNodes);
                }

                // Exit conditions 1
                boolean loadBalancingConstraint = earliestProcessorFinishTime + loadBalancedRemainingTime >= bestFinishTime;
                boolean criticalPathConstraint = earliestProcessorFinishTime + longestCriticalPath >= bestFinishTime;
                boolean latestFinishTimeConstraint = latestProcessorFinishTime >= bestFinishTime;
                if (loadBalancingConstraint || criticalPathConstraint || latestFinishTimeConstraint) {
                    state.candidateTasks.add(candidateTask);
                    continue;
                }

                // Update state (Location 1: Candidate Task)
                state.remainingDuration -= taskGraph.getDuration(candidateTask);
                List<Integer> candidateChildren = taskGraph.getChildrenList(candidateTask);
                for (Integer candidateChild : candidateChildren) {
                    state.inDegrees[candidateChild]--;
                    if (state.inDegrees[candidateChild] == 0) {
                        state.candidateTasks.add(candidateChild);
                    }
                }

                // Calculate information we need about constraints due to communication costs
                int maxDataArrival = 0;
                int processorCausingMaxDataArrival = 0;
                int secondMaxDataArrival = 0;
                List<Integer> parents = taskGraph.getParentsList(candidateTask);
                for (int parent : parents) {
                    int dataArrival = state.taskStartTimes[parent] + taskGraph.getDuration(parent) + taskGraph.getCommCost(parent, candidateTask);
                    if (dataArrival >= maxDataArrival) {
                        if (state.scheduledOn[parent] != processorCausingMaxDataArrival) {
                            secondMaxDataArrival = maxDataArrival;
                        }
                        maxDataArrival = dataArrival;
                        processorCausingMaxDataArrival = state.scheduledOn[parent];

                    } else if (dataArrival >= secondMaxDataArrival) {
                        if (state.scheduledOn[parent] != processorCausingMaxDataArrival) {
                            secondMaxDataArrival = dataArrival;
                        }
                    }
                }

                // Deep copy of candidateList is used in next recursive iteration
                boolean hasBeenScheduledAtStart = false;
                for (int candidateProcessor = 0; candidateProcessor < numProcessors; candidateProcessor++) { // Iterate through processors
                    // Avoid processor isomorphism
                    if (state.processorFinishTimes[candidateProcessor] == 0) {
                        if (hasBeenScheduledAtStart) {
                            // Skip duplicated search space
                            continue;
                        } else {
                            hasBeenScheduledAtStart = true;
                        }
                    }

                    // Find earliest time to schedule candidate task on candidate processor
                    int earliestStartTimeOnCurrentProcessor = state.processorFinishTimes[candidateProcessor];
                    if (processorCausingMaxDataArrival != candidateProcessor) {
                        earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, maxDataArrival);
                    } else {
                        earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, secondMaxDataArrival);
                    }

                    // Exit conditions 2: tighter constraint now that we have selected the processor
                    criticalPathConstraint = earliestStartTimeOnCurrentProcessor + maxLengthToExitNode[candidateTask] >= bestFinishTime;
                    if (criticalPathConstraint) {
                        continue;
                    }

                    // Update state (Location 2: Processors)
                    int prevFinishTime = state.processorFinishTimes[candidateProcessor];
                    state.processorFinishTimes[candidateProcessor] = earliestStartTimeOnCurrentProcessor + taskGraph.getDuration(candidateTask);
                    state.scheduledOn[candidateTask] = candidateProcessor;
                    state.taskStartTimes[candidateTask] = earliestStartTimeOnCurrentProcessor;

                    RecursiveSearch recursiveSearch;
                    recursiveSearch = new RecursiveSearch(state.getDeepCopy());
                    executableList.add(recursiveSearch);

                    // Backtrack state (Location 2: Processors)
                    state.processorFinishTimes[candidateProcessor] = prevFinishTime;
                }

                // Backtrack state (Location 1: Candidate Task)
                for (Integer candidateChild : candidateChildren) {
                    // revert changes made to children
                    state.inDegrees[candidateChild]++;
                    if (state.inDegrees[candidateChild] == 1) {
                        state.candidateTasks.removeLast();
                    }
                }
                state.remainingDuration += taskGraph.getDuration(candidateTask);
                state.candidateTasks.add(candidateTask);
                state.taskStartTimes[candidateTask] = -1;
                ForkJoinTask.invokeAll(executableList);
            }
        }

        /**
         * @param candidateTasks
         * @return
         */
        private LinkedList<Integer> toFTOList(LinkedList<Integer> candidateTasks) {
            int child = -1;
            int parentProcessor = -1;

            for (int task : candidateTasks) {
                // To be an FTO, every node must have at most one parent and at most one child
                if (taskGraph.getParentsList(task).size() > 1 || taskGraph.getChildrenList(task).size() > 1) {
                    return null;
                }

                // Every node must have the same child IF they have a child
                if (taskGraph.getChildrenList(task).size() > 0) {
                    int taskChild = taskGraph.getChildrenList(task).get(0);
                    if (child == -1) {
                        child = taskChild;
                    } else if (child != taskChild) {
                        return null;
                    }
                }

                // every node must have their parents on the same processor IF they have a parent.
                if (taskGraph.getParentsList(task).size() > 0) {
                    int taskParent = taskGraph.getParentsList(task).get(0);
                    int taskParentProcessor = state.scheduledOn[taskParent];
                    if (parentProcessor == -1) {
                        parentProcessor = taskParentProcessor;
                    } else if (parentProcessor != taskParentProcessor) {
                        return null;
                    }
                }
            }

            // sort by non-decreasing data ready time, i.e. finish time of parent + weight of edge
            sortByDataReadyTime(candidateTasks);

            // verify if the candidate tasks are ordered by out edge cost in non-increasing order,
            // if not we do not have a FTO.
            int prevOutEdgeCost = Integer.MAX_VALUE;
            for (int task : candidateTasks) {
                int edgeCost;
                if (taskGraph.getChildrenList(task).isEmpty()) {
                    // there is no out edge, cost is 0
                    edgeCost = 0;
                } else {
                    int taskChild = taskGraph.getChildrenList(task).get(0);
                    edgeCost = taskGraph.getCommCost(task, taskChild);
                }

                // if our current edge is larger than the previous edge, we don't have a FTO.
                if (edgeCost > prevOutEdgeCost) {
                    return null;
                } else {
                    prevOutEdgeCost = edgeCost;
                }
            }

            // we have a FTO!
            return candidateTasks;
        }

        private void getFTOSchedule(LinkedList<Integer> ftoSortedList) throws IOException, ClassNotFoundException {
            // Base case
            if (ftoSortedList.isEmpty()) {
                int finishTime = findMaxInArray(state.processorFinishTimes);

                // If schedule time is better, update bestFinishTime and best schedule
                if (finishTime < bestFinishTime) {
                    bestFinishTime = finishTime;

                    for (int i = 0; i < bestStartTime.length; i++) {
                        bestScheduledOn[i] = state.scheduledOn[i];
                        bestStartTime[i] = state.taskStartTimes[i];
                    }
                }
                return;
            }

            // Create a hash code for our partial schedule to check whether we have examined an equivalent schedule before
            // If we have seen an equivalent schedule we do not need to proceed
            int hashCode = PartialSchedule.generateHashCode(state.taskStartTimes, state.scheduledOn, numProcessors);
            if (seenSchedules.contains(hashCode)) {
                return;
            } else {
                seenSchedules.add(hashCode);
            }

            // Information we need about the current schedule
            // minimal remaining time IF all remaining tasks are evenly distributed amongst processors.
            int loadBalancedRemainingTime = (int) Math.ceil(state.remainingDuration / (double) numProcessors);

            int earliestProcessorFinishTime = Integer.MAX_VALUE;
            int latestProcessorFinishTime = 0;
            for (int l = 0; l < numProcessors; l++) {
                earliestProcessorFinishTime = Math.min(state.processorFinishTimes[l], earliestProcessorFinishTime);
                latestProcessorFinishTime = Math.max(state.processorFinishTimes[l], latestProcessorFinishTime);
            }

            int longestCriticalPath = 0;
            for (int task : ftoSortedList) {
                int criticalPath = maxLengthToExitNode[task];
                if (criticalPath > longestCriticalPath) {
                    longestCriticalPath = criticalPath;
                }
            }

            // Exit conditions 1
            boolean loadBalancingConstraint = earliestProcessorFinishTime + loadBalancedRemainingTime >= bestFinishTime;
            boolean criticalPathConstraint = earliestProcessorFinishTime + longestCriticalPath >= bestFinishTime;
            boolean latestFinishTimeConstraint = latestProcessorFinishTime >= bestFinishTime;
            if (loadBalancingConstraint || criticalPathConstraint || latestFinishTimeConstraint) {
                return;
            }

            //Update the state: Location 1
            LinkedList<Integer> duplicateCandidateTasks = new LinkedList<>(ftoSortedList);
            int firstTask = duplicateCandidateTasks.poll();
            state.remainingDuration -= taskGraph.getDuration(firstTask);


            boolean taskChildAdded = false;
            if (!taskGraph.getChildrenList(firstTask).isEmpty()) {
                int child = taskGraph.getChildrenList(firstTask).get(0);
                state.inDegrees[child]--;
                if (state.inDegrees[child] == 0) {
                    duplicateCandidateTasks.add(child);
                    taskChildAdded = true;
                }
            }

            // since we have a FTO, we can schedule the first task on all processors.
            boolean hasBeenScheduledAtStart = false;

            List<RecursiveSearch> executableList = new ArrayList<>();

            for (int candidateProcessor = 0; candidateProcessor < numProcessors; candidateProcessor++) {
                // Avoid processor isomorphism
                if (state.processorFinishTimes[candidateProcessor] == 0) {
                    if (hasBeenScheduledAtStart) {
                        // Skip duplicated search space
                        continue;
                    } else {
                        hasBeenScheduledAtStart = true;
                    }
                }

                // Find the min start time on this processor
                int earliestStartTimeOnCurrentProcessor = state.processorFinishTimes[candidateProcessor];
                if (!taskGraph.getParentsList(firstTask).isEmpty()) {
                    int parent = taskGraph.getParentsList(firstTask).get(0);
                    if (state.scheduledOn[parent] == candidateProcessor) {
                        earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, state.taskStartTimes[parent]
                                + taskGraph.getDuration(parent));
                    } else {
                        earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, state.taskStartTimes[parent]
                                + taskGraph.getDuration(parent) + taskGraph.getCommCost(parent, firstTask));
                    }
                }

                // Exit conditions 2: tighter constraint now that we have selected the processor
                criticalPathConstraint = earliestStartTimeOnCurrentProcessor + maxLengthToExitNode[firstTask] >= bestFinishTime;
                if (criticalPathConstraint) {
                    continue;
                }

                // Update the state: Location 2
                int prevFinishTime = state.processorFinishTimes[candidateProcessor];
                state.processorFinishTimes[candidateProcessor] = earliestStartTimeOnCurrentProcessor + taskGraph.getDuration(firstTask);
                state.scheduledOn[firstTask] = candidateProcessor;
                state.taskStartTimes[firstTask] = earliestStartTimeOnCurrentProcessor;

                if (!taskChildAdded) {
                    // it remains a FTO, we don't have to check again
                    getFTOSchedule(duplicateCandidateTasks);
                } else {
                    State duplicateState = state.getDeepCopy();
                    duplicateState.candidateTasks = duplicateCandidateTasks;
                    executableList.add(new RecursiveSearch(duplicateState));
                }

                // Backtrack: Location 2
                state.processorFinishTimes[candidateProcessor] = prevFinishTime;
            }
            // Backtrack: Location 1
            if(!taskGraph.getChildrenList(firstTask).isEmpty()) {
                int child = taskGraph.getChildrenList(firstTask).get(0);
                state.inDegrees[child]++;
            }
            state.remainingDuration += taskGraph.getDuration(firstTask);
            state.taskStartTimes[firstTask] = -1;

            ForkJoinTask.invokeAll(executableList);
        }

        private void sortByDataReadyTime(List<Integer> candidateTasks) {
            candidateTasks.sort((task1, task2) -> {
                int task1DataReadyTime = 0;
                int task2DataReadyTime = 0;

                if (!taskGraph.getParentsList(task1).isEmpty()) {
                    int parent = taskGraph.getParentsList(task1).get(0);
                    int commCost = taskGraph.getCommCost(parent, task1);
                    task1DataReadyTime = state.taskStartTimes[parent] + taskGraph.getDuration(parent) + commCost;
                }

                if (!taskGraph.getParentsList(task2).isEmpty()) {
                    int parent = taskGraph.getParentsList(task2).get(0);
                    int commCost = taskGraph.getCommCost(parent, task2);
                    task2DataReadyTime = state.taskStartTimes[parent] + taskGraph.getDuration(parent) + commCost;
                }

                if (task1DataReadyTime < task2DataReadyTime) {
                    return -1;
                }
                if (task1DataReadyTime > task2DataReadyTime) {
                    return 1;
                }

                // Data ready times are equal, break the tie using the out-edge cost
                int task1OutEdgeCost = 0;
                int task2OutEdgeCost = 0;
                if (!taskGraph.getChildrenList(task1).isEmpty()) {
                    int child = taskGraph.getChildrenList(task1).get(0);
                    task1OutEdgeCost = taskGraph.getCommCost(task1, child);
                }
                if (!taskGraph.getChildrenList(task2).isEmpty()) {
                    int child = taskGraph.getChildrenList(task2).get(0);
                    task2OutEdgeCost = taskGraph.getCommCost(task2, child);
                }

                if (task1OutEdgeCost > task2OutEdgeCost) {
                    return -1;
                }
                if (task1OutEdgeCost < task2OutEdgeCost) {
                    return 1;
                }
                //Data ready times and out-edge costs are equal
                return 0;
            });
        }
    }

    /**
     * Helper method to create the output Schedule.
     *
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
    private void initializeGlobalVars(TaskGraph taskGraph, int numProcessors, int upperBoundTime) {
        this.taskGraph = taskGraph;
        this.numProcessors = numProcessors;
        maxLengthToExitNode = PreProcessor.maxLengthToExitNode(taskGraph);
        nodePriorities = maxLengthToExitNode; //REFACTOR;
        bestFinishTime = upperBoundTime;
        numTasks = taskGraph.getNumberOfTasks();
        equivalentNodesList = PreProcessor.getNodeEquivalence(taskGraph); //REFACTOR
        bestStartTime = new int[numTasks];
        bestScheduledOn = new int[numTasks];
    }

    /**
     * Helper method to create the initial state on which the algorithm runs.
     */
    private State initializeState(TaskGraph taskGraph, int numProcessors) {
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
        return new State(candidateTasks, inDegrees, taskStartTimes,
                scheduledOn, processorFinishTimes, remainingDuration);
    }
}