package main;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class Solution {

    /**
     * Main method of the algorithm which schedules tasks on parallel processors
     * n is the number of tasks.
     * @param inList Array of list of vertices which can reach i-th vertex
     * @param outList Array of list of vertices reachable from i-th vertex
     * @param commCosts n by n array where the i,j -th element is the cost of going from i to j
     *                  Empty if no edge.
     * @param durations i-th element is the duration of task i.
     * @param numProcessors Number of processors.
     * @return List of scheduled tasks.
     */
    public Task[] run(List<Integer>[] inList, List<Integer>[] outList, int[][] commCosts, int[] durations, int numProcessors) {
        int n = inList.length;

        // create output array
        Task[] output = new Task[n];

        // i,j indicates earliest time to schedule task i on processor j
        int[][] earliestScheduleTimes = new int[n][numProcessors];

        // initialise array with in degree of vertices
        int[] inDegrees = new int[n];

        // initialise list of vertices with in degree = 0
        Queue<Integer> scheduleCandidates = new PriorityQueue<>();

        for (int i = 0; i < n; i++) {
            inDegrees[i] = inList[i].size();
            if (inDegrees[i] == 0) {
                scheduleCandidates.add(i);
            }
        }

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
            int finishTime = minStartTime + durations[candidate];
            output[candidate] = new Task(candidate, minStartTime, finishTime, minProcessor);

            // Update earliest schedule times for children
            for (int child: outList[candidate]) {
                for (int i = 0; i < numProcessors; i++) {
                    if (i == minProcessor) {
                        // for the processor the candidate was applied to,
                        // the earliest schedule time could be right after the candidate finishes
                        earliestScheduleTimes[child][minProcessor] = Math.max(finishTime,
                                earliestScheduleTimes[child][minProcessor]);
                    } else {
                        earliestScheduleTimes[child][i] = Math.max(finishTime + commCosts[candidate][child],
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

        return output;
    }
}
