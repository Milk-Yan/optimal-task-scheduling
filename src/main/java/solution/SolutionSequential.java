package solution;

import data.Schedule;
import data.Task;
import data.TaskGraph;
import solution.helpers.PartialScheduleHashGenerator;
import solution.helpers.PreProcessor;

import java.util.*;

public class SolutionSequential extends Solution {
    private int[] inDegrees; // inDegrees[i] => number of unscheduled parent tasks of task i
    private int[] taskStartTimes; // taskStartTimes[i] => start time of task i
    private int[] scheduledOn;  // scheduledOn[i] => the processor task i is scheduled on
    private int[] processorFinishTimes; // processorFinishTimes[i] => finishing time of the last task scheduled on processor i
    private int remainingDuration = 0; // total duration of remaining tasks to be scheduled (used for pruning)
    private int previousProcessor = -1;
    private boolean childAddedLastRound = false;

    /**
     * @param taskGraph      Graph containing tasks as nodes and their dependencies as edges.
     * @param numProcessors  Number of processors to schedule the tasks on.
     */
    public SolutionSequential (TaskGraph taskGraph, int numProcessors) {
        super(taskGraph, numProcessors);
    }

    /**
     * Creates an optimal scheduling of tasks on specified number of processors.
     * @return optimal schedule found by the run method.
     */
    public Schedule run() {
        LinkedList<Integer> candidateTasks = initialize(taskGraph, numProcessors);
        recursiveSearch(candidateTasks);
        setDone();
        return createOutput();
    }

    /**
     * This method recursively tries to schedule tasks on processors.
     *
     * @param candidateTasks a list of tasks that are currently available to be scheduled
     */
    private void recursiveSearch(LinkedList<Integer> candidateTasks) {
        updateStateCount();

        // Base case is when queue is empty, i.e. all tasks scheduled.
        if (candidateTasks.isEmpty()) {
            updateBestSchedule();
            return;
        }

        // Create a hash code for our partial schedule to check whether we have examined an equivalent schedule before
        // If we have seen an equivalent schedule we do not need to proceed
        HashSet<Integer> hashCodes = PartialScheduleHashGenerator.generateHashCode(taskStartTimes, scheduledOn, numProcessors);
        if (seenSchedules.contains(hashCodes)) {
            return;
        } else {
            // Find if we can complete the tasks in Fixed data.Task Order (FTO)
            LinkedList<Integer> ftoSorted = toFTOList(new LinkedList<>(candidateTasks));
            if (ftoSorted != null) {
                getFTOSchedule(ftoSorted);
                return;
            }
            seenSchedules.add(hashCodes);
        }

        // Information we need about the current schedule
        // minimal remaining time IF all remaining tasks are evenly distributed amongst processors.
        int loadBalancedRemainingTime = (int) Math.ceil(remainingDuration / (double) numProcessors);

        int earliestProcessorFinishTime = Integer.MAX_VALUE;
        int latestProcessorFinishTime = 0;
        for (int l = 0; l < numProcessors; l++) {
            earliestProcessorFinishTime = Math.min(processorFinishTimes[l], earliestProcessorFinishTime);
            latestProcessorFinishTime = Math.max(processorFinishTimes[l], latestProcessorFinishTime);
        }

        int longestCriticalPath = calculateLongestCriticalPath(candidateTasks);

        // Iterate through tasks
        candidateTasks.sort(Comparator.comparingInt(a -> nodePriorities[a]));
        HashSet<Integer> seenTasks = new HashSet<>();
        for (int i = 0; i < candidateTasks.size(); i++) {
            int candidateTask = candidateTasks.remove();

            // check for node duplication
            if (seenTasks.contains(candidateTask)) {
                candidateTasks.add(candidateTask);
                continue;
            } else {
                ArrayList<Integer> equivalentNodes = equivalentNodesList[candidateTask];
                seenTasks.addAll(equivalentNodes);
            }

            // if the our schedule can never become an optimal schedule, then there is no need to continue trying
            // this combination.
            if (!isPotentialOptimal(earliestProcessorFinishTime, loadBalancedRemainingTime, longestCriticalPath,
                    latestProcessorFinishTime)) {
                candidateTasks.add(candidateTask);
                continue;
            }

            // Update state (Location 1: Candidate data.Task)
            remainingDuration -= taskGraph.getDuration(candidateTask);
            boolean childAddedThisRound = false;
            List<Integer> candidateChildren = taskGraph.getChildrenList(candidateTask);
            for (Integer candidateChild : candidateChildren) {
                inDegrees[candidateChild]--;
                if (inDegrees[candidateChild] == 0) {
                    candidateTasks.add(candidateChild);
                    childAddedThisRound = true;
                }
            }

            // Calculate information we need about constraints due to communication costs
            int maxDataArrival = 0;
            int processorCausingMaxDataArrival = 0;
            int secondMaxDataArrival = 0;
            List<Integer> parents = taskGraph.getParentsList(candidateTask);
            for (int parent : parents) {
                int dataArrival = taskStartTimes[parent] + taskGraph.getDuration(parent) + taskGraph.getCommCost(parent, candidateTask);
                if (dataArrival >= maxDataArrival) {
                    if (scheduledOn[parent] != processorCausingMaxDataArrival) {
                        secondMaxDataArrival = maxDataArrival;
                    }
                    maxDataArrival = dataArrival;
                    processorCausingMaxDataArrival = scheduledOn[parent];

                } else if (dataArrival >= secondMaxDataArrival) {
                    if (scheduledOn[parent] != processorCausingMaxDataArrival) {
                        secondMaxDataArrival = dataArrival;
                    }
                }
            }


            // Deep copy of candidateList is used in next recursive iteration
            LinkedList<Integer> nextCandidateList = new LinkedList<Integer>(candidateTasks);
            boolean hasBeenScheduledAtStart = false;
            for (int candidateProcessor = 0; candidateProcessor < numProcessors; candidateProcessor++) { // Iterate through processors
                // Avoid processor isomorphism
                if (processorFinishTimes[candidateProcessor] == 0) {
                    if (hasBeenScheduledAtStart) {
                        // Skip duplicated search space
                        continue;
                    } else {
                        hasBeenScheduledAtStart = true;
                    }
                }

                // Partial duplicate avoidance
                if(!childAddedLastRound && candidateProcessor < previousProcessor){
                    continue;
                }

                // Find earliest time to schedule candidate task on candidate processor
                int earliestStartTimeOnCurrentProcessor = processorFinishTimes[candidateProcessor];
                if (processorCausingMaxDataArrival != candidateProcessor) {
                    earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, maxDataArrival);
                } else {
                    earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, secondMaxDataArrival);
                }

                // Pruning: tighter constraint now that we have selected the processor
                if (earliestStartTimeOnCurrentProcessor + maxLengthToExitNode[candidateTask] >= bestFinishTime) {
                    continue;
                }

                // Update state (Location 2: Processors)
                int prevFinishTime = processorFinishTimes[candidateProcessor];
                int oldPreviousProcessor = previousProcessor;
                boolean oldChildAddedLastRound = childAddedLastRound;
                previousProcessor = candidateProcessor;
                childAddedLastRound = childAddedThisRound;

                processorFinishTimes[candidateProcessor] = earliestStartTimeOnCurrentProcessor + taskGraph.getDuration(candidateTask);
                scheduledOn[candidateTask] = candidateProcessor;
                taskStartTimes[candidateTask] = earliestStartTimeOnCurrentProcessor;

                recursiveSearch(nextCandidateList);

                // Backtrack state (Location 2: Processors)
                processorFinishTimes[candidateProcessor] = prevFinishTime;
                previousProcessor = oldPreviousProcessor;
                childAddedLastRound = oldChildAddedLastRound;
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
            taskStartTimes[candidateTask] = -1;
        }
    }

    /**
     * Helper method to initialize all the fields required for the solution.
     */
    private LinkedList<Integer> initialize(TaskGraph taskGraph, int numProcessors) {
        this.taskGraph = taskGraph;
        this.numProcessors = numProcessors;

        maxLengthToExitNode = PreProcessor.maxLengthToExitNode(taskGraph);
        numTasks = taskGraph.getNumberOfTasks();

        nodePriorities = maxLengthToExitNode;
        equivalentNodesList = PreProcessor.getNodeEquivalence(taskGraph);

        inDegrees = new int[numTasks];
        bestStartTime = new int[numTasks];
        bestScheduledOn = new int[numTasks];
        processorFinishTimes = new int[numProcessors];
        taskStartTimes = new int[numTasks];
        Arrays.fill(taskStartTimes, -1);
        scheduledOn = new int[numTasks];
        LinkedList<Integer> candidateTasks = new LinkedList<>();

        for (int i = 0; i < numTasks; i++) {
            // calculate remaining duration of tasks to be scheduled
            remainingDuration += taskGraph.getDuration(i);
            inDegrees[i] = taskGraph.getParentsList(i).size();
            if (inDegrees[i] == 0) {
                candidateTasks.add(i);
            }
        }
        return candidateTasks;

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
     * Sorts the list of free tasks into Fixed data.Task Order if possible.
     * @param candidateTasks list of free tasks yet to be scheduled.
     * @return null if no FTO found, otherwise the FTO.
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
                int taskParentProcessor = scheduledOn[taskParent];
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


    /**
     * Sorts the list of candidate tasks by non-decreasing data ready time. When two data ready times
     * are equal, we use the non-increasing out-edge cost to break this tie.
     * Data ready time = finish time of parent + communication cost between parent and task.
     * Out-edge cost = communication cost between task and child.
     * @param candidateTasks the list of free tasks that are still unscheduled.
     */
    private void sortByDataReadyTime(List<Integer> candidateTasks) {
        candidateTasks.sort((task1, task2) -> {
            int task1DataReadyTime = 0;
            int task2DataReadyTime = 0;

            if (!taskGraph.getParentsList(task1).isEmpty()) {
                int parent = taskGraph.getParentsList(task1).get(0);
                int commCost = taskGraph.getCommCost(parent, task1);
                task1DataReadyTime = taskStartTimes[parent] + taskGraph.getDuration(parent) + commCost;
            }

            if (!taskGraph.getParentsList(task2).isEmpty()) {
                int parent = taskGraph.getParentsList(task2).get(0);
                int commCost = taskGraph.getCommCost(parent, task2);
                task2DataReadyTime = taskStartTimes[parent] + taskGraph.getDuration(parent) + commCost;
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

            return Integer.compare(task2OutEdgeCost, task1OutEdgeCost);
        });
    }

    /**
     * Given a Fixed data.Task Order sorted list, we know that we can safely schedule the next task.
     * This method will schedule in FTO order.
     * @param ftoSortedList the FTO sorted list.
     */
    private void getFTOSchedule(LinkedList<Integer> ftoSortedList) {
        updateStateCount();

        // Base case
        if (ftoSortedList.isEmpty()) {
            updateBestSchedule();
        }

        // Create a hash code for our partial schedule to check whether we have examined an equivalent schedule before
        // If we have seen an equivalent schedule we do not need to proceed
        HashSet<Integer> hashCodes = PartialScheduleHashGenerator.generateHashCode(taskStartTimes, scheduledOn, numProcessors);
        if (seenSchedules.contains(hashCodes)) {
            return;
        } else {
            seenSchedules.add(hashCodes);
        }

        // Information we need about the current schedule
        // minimal remaining time IF all remaining tasks are evenly distributed amongst processors.
        int loadBalancedRemainingTime = (int) Math.ceil(remainingDuration / (double) numProcessors);

        int earliestProcessorFinishTime = Integer.MAX_VALUE;
        int latestProcessorFinishTime = 0;
        for (int l = 0; l < numProcessors; l++) {
            earliestProcessorFinishTime = Math.min(processorFinishTimes[l], earliestProcessorFinishTime);
            latestProcessorFinishTime = Math.max(processorFinishTimes[l], latestProcessorFinishTime);
        }

        int longestCriticalPath = 0;
        for (int task : ftoSortedList) {
            int criticalPath = maxLengthToExitNode[task];
            if (criticalPath > longestCriticalPath) {
                longestCriticalPath = criticalPath;
            }
        }

        // Exit conditions 1
        if (!isPotentialOptimal(earliestProcessorFinishTime, loadBalancedRemainingTime, longestCriticalPath,
                latestProcessorFinishTime)) {
            return;
        }

        // Update the state: Location 1
        LinkedList<Integer> duplicate = new LinkedList<>(ftoSortedList);
        int firstTask = duplicate.poll();
        remainingDuration -= taskGraph.getDuration(firstTask);


        boolean taskChildAdded = false;
        if (!taskGraph.getChildrenList(firstTask).isEmpty()) {
            int child = taskGraph.getChildrenList(firstTask).get(0);
            inDegrees[child]--;
            if (inDegrees[child] == 0) {
                duplicate.add(child);
                taskChildAdded = true;
            }
        }

        // since we have a FTO, we can schedule the first task on all processors.
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

            // Find the min start time on this processor
            int earliestStartTimeOnCurrentProcessor = findEarliestStartTimeOnCurrentProcessor(candidateProcessor,
                    firstTask);

            // Exit conditions 2: tighter constraint now that we have selected the processor
            if (earliestStartTimeOnCurrentProcessor + maxLengthToExitNode[firstTask] >= bestFinishTime) {
                continue;
            }

            // Update the state: Location 2
            int prevFinishTime = processorFinishTimes[candidateProcessor];
            int oldPreviousProcessor = previousProcessor;
            boolean oldChildAddedLastRound = childAddedLastRound;
            previousProcessor = candidateProcessor;
            childAddedLastRound = taskChildAdded;

            processorFinishTimes[candidateProcessor] = earliestStartTimeOnCurrentProcessor + taskGraph.getDuration(firstTask);
            scheduledOn[firstTask] = candidateProcessor;
            taskStartTimes[firstTask] = earliestStartTimeOnCurrentProcessor;

            if (!taskChildAdded) {
                // it remains a FTO, we don't have to check again
                getFTOSchedule(duplicate);
            } else {
                recursiveSearch(duplicate);
            }

            // Backtrack: Location 2
            processorFinishTimes[candidateProcessor] = prevFinishTime;
            previousProcessor = oldPreviousProcessor;
            childAddedLastRound = oldChildAddedLastRound;
        }
        // Backtrack: Location 1
        if(!taskGraph.getChildrenList(firstTask).isEmpty()) {
            int child = taskGraph.getChildrenList(firstTask).get(0);
            inDegrees[child]++;
        }
        remainingDuration += taskGraph.getDuration(firstTask);
        taskStartTimes[firstTask] = -1;
    }

    /**
     * This method should be called when a schedule is created. We will update the best
     * schedule so far if the schedule is better.
     */
    private void updateBestSchedule() {
        int finishTime = findMaxInArray(processorFinishTimes);

        //If schedule time is better, update bestFinishTime and best schedule
        if (finishTime < bestFinishTime) {
            bestFinishTime = finishTime;

            for (int i = 0; i < bestStartTime.length; i++) {
                bestScheduledOn[i] = scheduledOn[i];
                bestStartTime[i] = taskStartTimes[i];
            }
            updateBestScheduleOnVisual();
        }
    }

    /**
     * Calculates the longest critical path amongst all the candidate tasks. This is a lower
     * bound on the finish time of this schedule.
     * @param candidateTasks the list of unscheduled free tasks.
     * @return the longest critical path length.
     */
    private int calculateLongestCriticalPath(List<Integer> candidateTasks) {
        int longestCriticalPath = 0;
        for (int task : candidateTasks) {
            int criticalPath = maxLengthToExitNode[task];
            if (criticalPath > longestCriticalPath) {
                longestCriticalPath = criticalPath;
            }
        }

        return longestCriticalPath;
    }

    /**
     * Checks if the current schedule can be optimal by comparing against the
     * current best finishing time. If it takes longer or equal time, the current
     * schedule can't be better than the current best schedule.
     * @return
     */
    private boolean isPotentialOptimal(int earliestProcessorFinishTime, int loadBalancedRemainingTime,
                                       int longestCriticalPath, int latestProcessorFinishTime) {
        boolean loadBalancingConstraint = earliestProcessorFinishTime + loadBalancedRemainingTime >= bestFinishTime;
        boolean criticalPathConstraint = earliestProcessorFinishTime + longestCriticalPath >= bestFinishTime;
        boolean latestFinishTimeConstraint = latestProcessorFinishTime >= bestFinishTime;

        if (loadBalancingConstraint || criticalPathConstraint || latestFinishTimeConstraint) {
            return false;
        }

        return true;
    }

    /**
     * Find the earliest start time on the current processor.
     * @param candidateProcessor the processor to check.
     * @param candidateTask the task to schedule.
     * @return earliest start time.
     */
    private int findEarliestStartTimeOnCurrentProcessor(int candidateProcessor, int candidateTask) {
        int earliestStartTimeOnCurrentProcessor = processorFinishTimes[candidateProcessor];
        if (!taskGraph.getParentsList(candidateTask).isEmpty()) {
            int parent = taskGraph.getParentsList(candidateTask).get(0);
            if (scheduledOn[parent] == candidateProcessor) {
                earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor,
                        taskStartTimes[parent] + taskGraph.getDuration(parent));
            } else {
                earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor,
                        taskStartTimes[parent] + taskGraph.getDuration(parent) +
                                taskGraph.getCommCost(parent, candidateTask));
            }
        }

        return earliestStartTimeOnCurrentProcessor;
    }
}
