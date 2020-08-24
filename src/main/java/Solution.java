import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Solution {
    private TaskGraph taskGraph;
    private int numProcessors;

    private int[] inDegrees; // inDegrees[i] => number of unscheduled parent tasks of task i
    private int[] startTimes; // startTimes[i] => start time of task i
    private int[] scheduledOn;  // scheduledOn[i] => the processor task i is scheduled on (-1 if not scheduled)
    private int[] processorFinishTimes; // processorAvailability[i] => finishing time of the last task scheduled on processor i
    private int remainingDuration = 0; // total duration of remaining tasks to be scheduled (used for pruning)

    private int[] bestStartTime; // bestStartTime[i] => start time of task i in best schedule found so far
    private int[] bestScheduledOn; // bestScheduledOn[i] => processor that task i is scheduled on, in best schedule
    private int bestTime; // earliest finishing time of schedules we have searched

    LinkedList<Integer> scheduleCandidates; // queue of tasks with no unprocessed dependencies

    /**
     * Creates an optimal scheduling of tasks on specified number of processors.
     * @param taskGraph Graph containing tasks as nodes and their dependencies as edges.
     * @param numProcessors Number of processors to schedule the tasks on.
     * @param upperBoundTime Upper bound of running time that the optimal solution should do at least as good as.
     * @return optimal schedule found by the run method.
     */
    public Task[] run(TaskGraph taskGraph, int numProcessors, int upperBoundTime) {
        // initialisation of fields
        this.taskGraph = taskGraph;
        this.numProcessors = numProcessors;
        this.bestTime = upperBoundTime;
        int n = taskGraph.getNumberOfTasks();
        inDegrees = new int[n];
        bestStartTime = new int[n];
        bestScheduledOn = new int[n];
        this.numProcessors = numProcessors;
        processorFinishTimes = new int[numProcessors];
        startTimes = new int[n];
        scheduledOn = new int[n];
        Arrays.fill(scheduledOn, -1);
        scheduleCandidates = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            // calculate remaining duration of tasks to be scheduled
            remainingDuration += taskGraph.getDuration(i);
            inDegrees[i] = taskGraph.getParentsList(i).size();
            if (inDegrees[i] == 0) {
                scheduleCandidates.add(i);
            }
        }

        recursiveSearch();

        // create output task array
        Task[] optimalSchedule = new Task[n];
        for (int i = 0; i < n; i++) {
            Task t = new Task(i, bestStartTime[i],
                    bestStartTime[i] + taskGraph.getDuration(i), bestScheduledOn[i]);
            optimalSchedule[i] = t;
        }
        return optimalSchedule;
    }

    /**
     * Recursively try to schedule a task on a processor.
     * Uses DFS to try all possible schedules.
     */
    private void recursiveSearch() {
        // base case is when queue is empty
        // this means we have scheduled all tasks
        if (scheduleCandidates.size() == 0) {
            // find finishing time of current schedule
            int finishTime = Integer.MIN_VALUE;
            for (int i = 0; i < numProcessors; i++) {
                finishTime = Math.max(processorFinishTimes[i], finishTime);
            }

            // check if this schedule has the best time
            if (finishTime <= bestTime) {
                bestTime = finishTime;

                // update the best schedule
                for (int i = 0; i < bestStartTime.length; i++) {
                    bestScheduledOn[i] = scheduledOn[i];
                    bestStartTime[i] = startTimes[i];
                }
            }
            return;
        }

        // In the best case, all remaining tasks will be evenly distributed amongst all the processors,
        // giving the minimum remaining time.
        int minRemainingTime = (int)Math.ceil(remainingDuration /(double) numProcessors);

        // find the processor which finishes earliest in current schedule
        int earliestProcessorFinishTime = Integer.MAX_VALUE;
        for (int l = 0; l < numProcessors; l++) {
            earliestProcessorFinishTime = Math.min(processorFinishTimes[l], earliestProcessorFinishTime);
        }

        // only continue processing this state if it is possible to do better than the current best time we have found
        if(earliestProcessorFinishTime + minRemainingTime < bestTime) {

            // recursively try different schedules
            for (int i = 0; i < scheduleCandidates.size(); i++) {
                // get task to try schedule
                int candidateTask = scheduleCandidates.remove();
                // remaining tasks to schedule does not include candidate task
                remainingDuration -= taskGraph.getDuration(candidateTask);

                // get children of candidate task
                List<Integer> candidateChildren = taskGraph.getChildrenList(candidateTask);

                // update in degrees of children of candidate
                for (Integer candidateChild : candidateChildren) {
                    inDegrees[candidateChild]--;
                    // add child to queue if it has no more parents to process
                    if (inDegrees[candidateChild] == 0) {
                        scheduleCandidates.add(candidateChild);
                    }
                }

                // whether this task has been scheduled at time 0 on a processor before
                // used to prune tree
                boolean scheduledOnZero = false;

                // schedule candidate task on each processor
                for (int candidateProcessor = 0; candidateProcessor < numProcessors; candidateProcessor++) {

                    // check if this task is being scheduled at time 0 on candidate processor
                    if (processorFinishTimes[candidateProcessor] == 0) {
                        // check if we have scheduled this task at time 0 on another processor before
                        // if so, this state has already been checked
                        if (scheduledOnZero) {
                            continue;
                        } else {
                            scheduledOnZero = true;
                        }
                    }

                    List<Integer> parents = taskGraph.getParentsList(candidateTask);

                    // find the earliest time the candidate task can be scheduled on the candidate processor
                    int earliestStartTime = Integer.MIN_VALUE;
                    for (int parent : parents) {
                        // check constraints due to comm costs of parents on other processors
                        if (scheduledOn[parent] != candidateProcessor) {
                            earliestStartTime = Math.max(earliestStartTime, startTimes[parent] +
                                    taskGraph.getDuration(parent) + taskGraph.getCommCost(parent, candidateTask));
                        }
                    }
                    // check constraint of latest finishing time of task on candidate processor
                    earliestStartTime = Math.max(earliestStartTime, processorFinishTimes[candidateProcessor]);

                    // store finish time of candidate processor before scheduling candidate task
                    // used for backtracking
                    int prevFinishTime = processorFinishTimes[candidateProcessor];

                    // update finish time of candidate processor to finishing time of scheduled candidate task
                    processorFinishTimes[candidateProcessor] = earliestStartTime + taskGraph.getDuration(candidateTask);

                    // update processor the candidate task is scheduled on
                    scheduledOn[candidateTask] = candidateProcessor;

                    // update start time of candidate task
                    startTimes[candidateTask] = earliestStartTime;

                    // recursive step
                    recursiveSearch();

                    // revert candidate processor's finish time
                    processorFinishTimes[candidateProcessor] = prevFinishTime;
                }

                // backtracking
                // remove scheduling of candidate task
                scheduledOn[candidateTask] = -1;
                // revert totalSum to include candidate task
                remainingDuration += taskGraph.getDuration(candidateTask);
                for (Integer candidateChild : candidateChildren) {
                    // revert changes made to children
                    inDegrees[candidateChild]++;
                    if (inDegrees[candidateChild] == 1) {
                        scheduleCandidates.removeLast();
                    }
                }
                // add candidate task back to queue since it is now unscheduled
                scheduleCandidates.add(candidateTask);
            }
        }
    }
}
