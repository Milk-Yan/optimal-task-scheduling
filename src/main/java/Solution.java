import java.util.*;

public class Solution {
    private TaskGraph taskGraph;
    private int numProcessors;
    private int numTasks;

    private int[] maxLengthToExitNode;
    private int[] inDegrees; // inDegrees[i] => number of unscheduled parent tasks of task i
    private int[] taskStartTimes; // taskStartTimes[i] => start time of task i
    private int[] scheduledOn;  // scheduledOn[i] => the processor task i is scheduled on
    private int[] processorFinishTimes; // processorFinishTimes[i] => finishing time of the last task scheduled on processor i
    private int remainingDuration = 0; // total duration of remaining tasks to be scheduled (used for pruning)

    private int[] nodePriorities;
    private int[] bestStartTime; // bestStartTime[i] => start time of task i in best schedule found so far
    private int[] bestScheduledOn; // bestScheduledOn[i] => processor that task i is scheduled on, in best schedule
    private int bestFinishTime; // earliest finishing time of schedules we have searched

    HashSet<Integer> seenSchedules = new HashSet<>();

    /**
     * Creates an optimal scheduling of tasks on specified number of processors.
     *
     * @param taskGraph      Graph containing tasks as nodes and their dependencies as edges.
     * @param numProcessors  Number of processors to schedule the tasks on.
     * @param upperBoundTime Upper bound of running time that the optimal solution should do at least as good as.
     * @return optimal schedule found by the run method.
     */
    public Schedule run(TaskGraph taskGraph, int numProcessors, int upperBoundTime) {
        LinkedList<Integer> candidateTasks = initialize(taskGraph, numProcessors, upperBoundTime);
        nodePriorities = maxLengthToExitNode;
        recursiveSearch(candidateTasks);
        return createOutput();
    }

    /**
     * Recursively try to schedule a task on a processor.
     * Uses DFS to try all possible schedules.
     */
    private void recursiveSearch(LinkedList<Integer> candidateTasks) {
        // Base case is when queue is empty, i.e. all tasks scheduled.
        if (candidateTasks.isEmpty()) {
            int finishTime = findMaxInArray(processorFinishTimes);

            //If schedule time is better, update bestFinishTime and best schedule
            if (finishTime < bestFinishTime) {
                bestFinishTime = finishTime;

                for (int i = 0; i < bestStartTime.length; i++) {
                    bestScheduledOn[i] = scheduledOn[i];
                    bestStartTime[i] = taskStartTimes[i];
                }
            }
            return;
        }

        // Create a hash code for our partial schedule to check whether we have examined an equivalent schedule before
        // If we have seen an equivalent schedule we do not need to proceed
        int hashCode = PartialSchedule.generateHashCode(taskStartTimes, scheduledOn, numProcessors);
        if (seenSchedules.contains(hashCode)) {
            return;
        } else {
            seenSchedules.add(hashCode);
        }

        // Find if we can complete the tasks in Fixed Task Order (FTO)
        LinkedList<Integer> ftoSorted = toFTOList(new LinkedList<>(candidateTasks));
        if (ftoSorted != null) {
            getFTOSchedule(candidateTasks);
            return;
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
        for(int task : candidateTasks){
            int criticalPath = maxLengthToExitNode[task];
            if (criticalPath > longestCriticalPath){
                longestCriticalPath = criticalPath;
            }
        }


        // Iterate through tasks
        candidateTasks.sort(Comparator.comparingInt(a -> nodePriorities[a]));
        for (int i = 0; i < candidateTasks.size(); i++) {
            int candidateTask = candidateTasks.remove();

            // Exit conditions 1
            boolean loadBalancingConstraint = earliestProcessorFinishTime + loadBalancedRemainingTime >= bestFinishTime;
            boolean criticalPathConstraint = earliestProcessorFinishTime + longestCriticalPath >= bestFinishTime;
            boolean latestFinishTimeConstraint = latestProcessorFinishTime >= bestFinishTime;
            if (loadBalancingConstraint || criticalPathConstraint || latestFinishTimeConstraint) {
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

                // Find earliest time to schedule candidate task on candidate processor
                int earliestStartTimeOnCurrentProcessor = processorFinishTimes[candidateProcessor];
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
                int prevFinishTime = processorFinishTimes[candidateProcessor];
                processorFinishTimes[candidateProcessor] = earliestStartTimeOnCurrentProcessor + taskGraph.getDuration(candidateTask);
                scheduledOn[candidateTask] = candidateProcessor;
                taskStartTimes[candidateTask] = earliestStartTimeOnCurrentProcessor;

                recursiveSearch(nextCandidateList);

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
            taskStartTimes[candidateTask] = -1;
        }
    }

    /**
     * Helper method to initialize all the fields required for the solution.
     */
    private LinkedList<Integer> initialize(TaskGraph taskGraph, int numProcessors, int upperBoundTime) {
        this.taskGraph = taskGraph;
        this.numProcessors = numProcessors;

        maxLengthToExitNode = PreProcessor.maxLengthToExitNode(taskGraph);
        bestFinishTime = upperBoundTime;
        numTasks = taskGraph.getNumberOfTasks();

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
        for (int i = 0; i < arr.length; i++) {
            max = Math.max(max, arr[i]);
        }

        return max;
    }

    /**
     * @param candidateTasks
     * @return
     */
    public LinkedList<Integer> toFTOList(LinkedList<Integer> candidateTasks) {
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
        for (int task: candidateTasks) {
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


    private void sortByDataReadyTime(List<Integer> candidateTasks) {
        int[] dataReadyTimes = new int[numTasks];
        for(int candidate : candidateTasks){
            if (taskGraph.getParentsList(candidate).size() > 0) {
                int parent = taskGraph.getParentsList(candidate).get(0);
                int commCost = taskGraph.getCommCost(parent, candidate);
                dataReadyTimes[candidate] = taskStartTimes[parent] + taskGraph.getDuration(parent) + commCost;
            }
        }

        candidateTasks.sort(Comparator.comparingInt(a -> dataReadyTimes[a]));
    }

    public void getFTOSchedule(LinkedList<Integer> ftoSortedList) {
        // Base case
        if(ftoSortedList.isEmpty()){
            int finishTime = findMaxInArray(processorFinishTimes);

            // If schedule time is better, update bestFinishTime and best schedule
            if (finishTime < bestFinishTime) {
                bestFinishTime = finishTime;

                for (int i = 0; i < bestStartTime.length; i++) {
                    bestScheduledOn[i] = scheduledOn[i];
                    bestStartTime[i] = taskStartTimes[i];
                }
            }
            return;
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
        for(int task : ftoSortedList){
            int criticalPath = maxLengthToExitNode[task];
            if (criticalPath > longestCriticalPath){
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
        int firstTask = ftoSortedList.poll();
        LinkedList<Integer> duplicate = new LinkedList<>(ftoSortedList);
        remainingDuration -= taskGraph.getDuration(firstTask);

        boolean taskHasChild = !taskGraph.getChildrenList(firstTask).isEmpty();
        if (taskHasChild) {
            int child = taskGraph.getChildrenList(firstTask).get(0);
            inDegrees[child]--;
            duplicate.add(child);
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
            int earliestStartTimeOnCurrentProcessor = processorFinishTimes[candidateProcessor];
            if(!taskGraph.getParentsList(firstTask).isEmpty()){
                int parent = taskGraph.getParentsList(firstTask).get(0);
                earliestStartTimeOnCurrentProcessor = Math.max(earliestStartTimeOnCurrentProcessor, taskStartTimes[parent] + taskGraph.getDuration(parent)
                        + taskGraph.getCommCost(parent, firstTask));
            }

            // Exit conditions 2: tighter constraint now that we have selected the processor
            criticalPathConstraint = earliestStartTimeOnCurrentProcessor + maxLengthToExitNode[firstTask] >= bestFinishTime;
            if (criticalPathConstraint) {
                continue;
            }

            // Update the state: Location 2
            int prevFinishTime = processorFinishTimes[candidateProcessor];
            processorFinishTimes[candidateProcessor] = earliestStartTimeOnCurrentProcessor + taskGraph.getDuration(firstTask);
            scheduledOn[firstTask] = candidateProcessor;
            taskStartTimes[firstTask] = earliestStartTimeOnCurrentProcessor;

            if (!taskHasChild) {
                // it remains a FTO, we don't have to check again
                getFTOSchedule(duplicate);
            } else {
                recursiveSearch(duplicate);
            }

            // Backtrack: Location 2
            processorFinishTimes[candidateProcessor] = prevFinishTime;
        }

        // Backtrack: Location 1
        if(taskHasChild) {
            int child = taskGraph.getChildrenList(firstTask).get(0);
            inDegrees[child]++;
            duplicate.removeLast();
        }
        remainingDuration += taskGraph.getDuration(firstTask);
        duplicate.add(firstTask);
        taskStartTimes[firstTask] = -1;
    }
}