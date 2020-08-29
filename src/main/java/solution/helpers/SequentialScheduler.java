package solution.helpers;

import data.Schedule;
import data.Task;
import data.TaskGraph;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This is a scheduler only used when there is one processor. In
 * this case, we will return a schedule where the maximum finish
 * time will be the sum of all nodes, and the nodes will be arranged
 * such that all parents finish before their children.
 */
public class SequentialScheduler {
    private TaskGraph taskGraph; //Contains information about the DAG

    public SequentialScheduler(TaskGraph taskGraph) {
        this.taskGraph = taskGraph;
    }

    /**
     * This method gets the optimal solution for when the number of processors is exactly 1.
     *
     * @return Schedule, the optimal schedule when only one processor can be used to schedule tasks on.
     */
    public Schedule getSchedule() {
        int numTasks = taskGraph.getNumberOfTasks();
        int finishTime = 0;
        Task[] tasks = new Task[numTasks];
        int[] inDegrees = new int[numTasks];
        Queue<Integer> candidateTasks = new LinkedList<>();

        // initialize all the inDegrees of the tasks and put them
        // as a candidate if they have don't have any dependencies.
        for (int task = 0; task < numTasks; task++) {
            int inDegree = taskGraph.getParentsList(task).size();
            if (inDegree == 0) {
                candidateTasks.add(task);
            }
            inDegrees[task] = inDegree;
        }

        while (!candidateTasks.isEmpty()) {
            int task = candidateTasks.poll();
            // the task will start at the finish time of the previous task.
            int startTime = finishTime;
            // update the finish time to include the scheduled task.
            finishTime += taskGraph.getDuration(task);
            tasks[task] = new Task(startTime, finishTime, 0);

            // since this task is done, its children would
            // no longer have this dependency
            for (int child: taskGraph.getChildrenList(task)) {
                inDegrees[child]--;
                if (inDegrees[child] == 0) {
                    candidateTasks.add(child);
                }
            }
        }

        return new Schedule(tasks, finishTime);
    }
}
