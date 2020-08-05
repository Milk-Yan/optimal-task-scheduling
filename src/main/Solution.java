package main;

import java.util.List;

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
    public Task[] run(List<int[]> inList, List<int[]> outList, int[][] commCosts, int[] durations, int numProcessors) {
        // TODO
        // create output array
        // create list of tasks for each processor
        // initialise array with in degree of vertices

        // initialise list of vertices with in degree = 0

        // find a node with in degree 0

        // schedule it
        return null;
    }
}
