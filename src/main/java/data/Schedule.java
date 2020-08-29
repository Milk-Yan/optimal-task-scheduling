package data;

/**
 * Represents a schedule of the tasks from the input graph.
 */
public class Schedule {
    private final Task[] tasks;
    private final int finishTime;

    /**
     * Creates a Schedule representation of the tasks created for
     * a valid schedule.
     * @param tasks An array of tasks that are scheduled.
     * @param finishTime The finish time of the schedule.
     */
    public Schedule(Task[] tasks, int finishTime) {
        this.tasks = tasks;
        this.finishTime = finishTime;
    }

    /**
     * @return The tasks that are in the schedule.
     */
    public Task[] getTasks() {
        return tasks;
    }

    /**
     * @return The finish time (last finish time among all tasks) of
     * the schedule.
     */
    public int getFinishTime() {
        return finishTime;
    }
}
