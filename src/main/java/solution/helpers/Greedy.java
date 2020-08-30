package solution.helpers;

import data.Schedule;
import data.Task;
import data.TaskGraph;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class Greedy {

    public Schedule run(TaskGraph taskGraph, int numProcessors){
        Schedule bestSchedule = runSchedule(taskGraph, numProcessors);

        int[] bLevels = PreProcessor.maxLengthToExitNode(taskGraph);
        int[] weights = taskGraph.getDurations();

        Schedule reverseBLevelPriority = runScheduleWithPriority(taskGraph, numProcessors, bLevels, true);
        if(reverseBLevelPriority.getFinishTime() < bestSchedule.getFinishTime()) {
            bestSchedule = reverseBLevelPriority;
        }

        Schedule bLevelPriority = runScheduleWithPriority(taskGraph, numProcessors, bLevels, false);
        if(bLevelPriority.getFinishTime() < bestSchedule.getFinishTime()){
            bestSchedule = bLevelPriority;
        }

        Schedule reverseTaskWeightPriority = runScheduleWithPriority(taskGraph, numProcessors, weights, true);
        if(reverseTaskWeightPriority.getFinishTime() < bestSchedule.getFinishTime()){
            bestSchedule = reverseTaskWeightPriority;
        }

        Schedule taskWeightPriority = runScheduleWithPriority(taskGraph, numProcessors, weights, false);
        if(taskWeightPriority.getFinishTime() < bestSchedule.getFinishTime()){
            bestSchedule = taskWeightPriority;
        }

        return bestSchedule;
    }

    /**
     * Main method of the algorithm which schedules tasks on parallel processors
     * n is the number of tasks.
     * @param taskGraph object that encapsulates tasks and their dependencies.
     * @param numProcessors Number of processors.
     * @return List of scheduled tasks.
     */
    public Schedule runSchedule(TaskGraph taskGraph, int numProcessors) {
        int n = taskGraph.getNumberOfTasks();
        int finalFinishTime = 0;
        Task[] output = new Task[n];

        // scheduleCandidates contains all possible tasks which are able to be scheduled
        Queue<Integer> scheduleCandidates = new LinkedList<>();

        // earliestScheduleTimes[i][j] => earliest possible time to schedule task i on processor j
        int[][] earliestScheduleTimes = new int[n][numProcessors];

        // Set up the number of parents (in-degrees) of each task. If a task has no parent, then
        // we add it to the scheduleCandidates queue.
        int[] inDegrees = new int[n];
        for (int i = 0; i < n; i++) {
            inDegrees[i] = taskGraph.getParentsList(i).size();
            if (inDegrees[i] == 0) {
                scheduleCandidates.add(i);
            }
        }

        // repeat until all tasks have been scheduled (and a viable solution is found)
        while (!scheduleCandidates.isEmpty()) {
            // find a node with in degree 0
            int candidate = scheduleCandidates.poll();

            // Choose processor to schedule task on
            int minStartTime = earliestScheduleTimes[candidate][0];
            int minProcessor = 0;
            for (int i = 1; i < numProcessors; i++) {
                int currStartTime = earliestScheduleTimes[candidate][i];
                if (currStartTime < minStartTime) {
                    minStartTime = currStartTime;
                    minProcessor = i;
                }
            }

            // schedule task
            int finishTime = minStartTime + taskGraph.getDuration(candidate);
            finalFinishTime = Math.max(finalFinishTime, finishTime);

            output[candidate] = new Task(minStartTime, finishTime, minProcessor);

            // Update earliest schedule times for children
            for (int child: taskGraph.getChildrenList(candidate)) {
                for (int i = 0; i < numProcessors; i++) {
                    if (i == minProcessor) {
                        // for the processor the candidate was applied to,
                        // the earliest schedule time could be right after the candidate finishes
                        earliestScheduleTimes[child][minProcessor] = Math.max(finishTime,
                                earliestScheduleTimes[child][minProcessor]);
                    } else {
                        earliestScheduleTimes[child][i] = Math.max(finishTime + taskGraph.getCommCost(candidate, child),
                                earliestScheduleTimes[child][i]);
                    }
                }

                // Decrement in-degree count of child and see if it can be a candidate
                inDegrees[child]--;
                if (inDegrees[child] == 0) {
                    scheduleCandidates.add(child);
                }
            }
            // Update earliest schedule times for the processor which the task was scheduled on (minProcessor)
            for (int i = 0; i < n; i++) {
                earliestScheduleTimes[i][minProcessor] = Math.max(finishTime, earliestScheduleTimes[i][minProcessor]);
            }
        }

        return new Schedule(output, finalFinishTime);
    }

    public Schedule runScheduleWithPriority(TaskGraph taskGraph, int numProcessors, int[] nodePriorities, boolean reversed) {
        int reverse = reversed ? -1 : 1;

        int n = taskGraph.getNumberOfTasks();
        int finalFinishTime = 0;
        Task[] output = new Task[n];

        // scheduleCandidates contains all possible tasks which are able to be scheduled
        PriorityQueue<Integer> scheduleCandidates = new PriorityQueue<>(Comparator.comparingInt(a -> reverse * nodePriorities[a]));

        // earliestScheduleTimes[i][j] => earliest possible time to schedule task i on processor j
        int[][] earliestScheduleTimes = new int[n][numProcessors];

        // Set up the number of parents (in-degrees) of each task. If a task has no parent, then
        // we add it to the scheduleCandidates queue.
        int[] inDegrees = new int[n];
        for (int i = 0; i < n; i++) {
            inDegrees[i] = taskGraph.getParentsList(i).size();
            if (inDegrees[i] == 0) {
                scheduleCandidates.add(i);
            }
        }

        // repeat until all tasks have been scheduled (and a viable solution is found)
        while (!scheduleCandidates.isEmpty()) {
            // find a node with in degree 0
            int candidate = scheduleCandidates.poll();

            // Choose processor to schedule task on
            int minStartTime = earliestScheduleTimes[candidate][0];
            int minProcessor = 0;
            for (int i = 1; i < numProcessors; i++) {
                int currStartTime = earliestScheduleTimes[candidate][i];
                if (currStartTime < minStartTime) {
                    minStartTime = currStartTime;
                    minProcessor = i;
                }
            }

            // schedule task
            int finishTime = minStartTime + taskGraph.getDuration(candidate);
            finalFinishTime = Math.max(finalFinishTime, finishTime);

            output[candidate] = new Task(minStartTime, finishTime, minProcessor);

            // Update earliest schedule times for children
            for (int child: taskGraph.getChildrenList(candidate)) {
                for (int i = 0; i < numProcessors; i++) {
                    if (i == minProcessor) {
                        // for the processor the candidate was applied to,
                        // the earliest schedule time could be right after the candidate finishes
                        earliestScheduleTimes[child][minProcessor] = Math.max(finishTime,
                                earliestScheduleTimes[child][minProcessor]);
                    } else {
                        earliestScheduleTimes[child][i] = Math.max(finishTime + taskGraph.getCommCost(candidate, child),
                                earliestScheduleTimes[child][i]);
                    }
                }

                // Decrement in-degree count of child and see if it can be a candidate
                inDegrees[child]--;
                if (inDegrees[child] == 0) {
                    scheduleCandidates.add(child);
                }
            }
            // Update earliest schedule times for the processor which the task was scheduled on (minProcessor)
            for (int i = 0; i < n; i++) {
                earliestScheduleTimes[i][minProcessor] = Math.max(finishTime, earliestScheduleTimes[i][minProcessor]);
            }
        }

        return new Schedule(output, finalFinishTime);
    }
}
