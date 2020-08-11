package test;

import main.Task;

import java.util.List;

/**
 * Utility class to check if a solution is valid given the constraints
 * of preferences.
 */
public class ValidateSolution {

    /**
     * Finds if the input Task graph is a valid set of solutions.
     * @param inList Array of list of vertices which can reach i-th vertex
     * @param commCosts n by n array where the i,j -th element is the cost of going from i to j,
     *                  0 if no edge.
     * @param durations i-th element is the duration of task i.
     * @param numProcessors Number of processors.
     * @param solution List of scheduled tasks.
     * @return True if valid, False if invalid.
     */
    public boolean validate(List<Integer>[] inList, int[][] commCosts, int[] durations, int numProcessors, Task[] solution) {
        if (solution == null || solution.length == 0) {
            if (durations == null || durations.length == 0) {
                return true;
            } else {
                System.out.println("solution is empty but duration isn't");
                return false;
            }
        }

        // check if the number of tasks is equal
        if (solution.length != durations.length) {
            System.out.println("solution size is " + solution.length + ", but we should have " + durations.length + " tasks.");
            return false;
        }

        // the processors that we can check if it is occupied.
        Processor[] processors = new Processor[numProcessors];
        // initialise processors
        for (int i = 0; i < numProcessors; i++) {
            processors[i] = new Processor();
        }

        for (Task task: solution) {
            if (task == null) {
                System.out.println("no task found");
                return false;
            }

            if (processors[task.getProcessor()].isOccupied(task)) {
                System.out.println("Task that is occupied already during this time frame is " +
                        " getting used.");
                return false;
            } else {
                processors[task.getProcessor()].add(task);
            }

            if (!parentsCompleteBeforeTask(task, commCosts)) {
                System.out.println("Parents are not complete before task.");
                return false;
            }

            // make sure that the task is actually complete
            if (task.getFinishTime()-task.getStartTime() != durations[task.getId()]) {
                System.out.println("The task " + task.getId() + " is supposed to have a duration of " +
                        durations[task.getId()] + " but instead has start time " + task.getStartTime() +
                        " and end time: " + task.getFinishTime());
                return false;
            }

            // initiating list of parents of node
            List<Integer> parents = inList[task.getId()];
            if (parents == null) {
                continue;
            }

            for (int parent: parents) {
                for (Task parentTask: solution) {
                    if (parent == parentTask.getId()) {
                        task.addParent(parentTask);
                        break;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Find if all the parents of the task (all the nodes are ingoing edges
     * to the task) are complete before the task. Also counts for the difference
     * in transfer time in between processors.
     * @param task The task whose parents to check if they are done
     * @return Whether the parents have been completed before the task
     */
    private boolean parentsCompleteBeforeTask(Task task, int[][] commCosts) {
        List<Task> parents = task.getParents();

        for (Task parent: parents) {
            int commCost = 0;
            if (parent.getProcessor() != task.getProcessor()) {
                // if processors are different, we need to add comm costs
                commCost = commCosts[parent.getId()][task.getId()];
            }

            if ((parent.getFinishTime() + commCost) <= task.getStartTime()) {
                System.out.println("Parent " + parent.getId() + "finished at " + parent.getFinishTime() + " but task starts at " + task.getStartTime());
                return false;
            }
        }

        return true;
    }
}
