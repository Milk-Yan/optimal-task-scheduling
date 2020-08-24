import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class Greedy {

    /**
     * Main method of the greedy algorithm which schedules tasks on parallel processors
     * n is the number of tasks.
     * @param taskGraph object that encapsulates tasks and their dependencies.
     * @param numProcessors Number of processors.
     * @return finish time of the created schedule.
     */
    public int run(TaskGraph taskGraph, int numProcessors) {
        int n = taskGraph.getNumberOfTasks();

        // i,j indicates earliest time to schedule task i on processor j
        int[][] earliestScheduleTimes = new int[n][numProcessors];

        // initialise array with in degree of vertices
        int[] inDegrees = new int[n];

        // initialise list of vertices with in degree = 0
        Queue<Integer> scheduleCandidates = new PriorityQueue<>();

        for (int i = 0; i < n; i++) {
            inDegrees[i] = taskGraph.getParentsList(i).size();
            if (inDegrees[i] == 0) {
                scheduleCandidates.add(i);
            }
        }

        // return value representing the finishing time of the schedule
        int scheduleFinishTime = Integer.MIN_VALUE;

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

            // Schedule task
            int finishTime = minStartTime + taskGraph.getDuration(candidate);

            // Update finishing time of schedule if this is the last task to finish
            scheduleFinishTime = Math.max(finishTime, scheduleFinishTime);

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

        return scheduleFinishTime;
    }
}
