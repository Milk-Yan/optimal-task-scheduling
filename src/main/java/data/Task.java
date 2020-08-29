package data;

/**
 * Class to keep track of all information related to a scheduled task
 */
public class Task implements Comparable<Task> {
    private final int startTime;
    private final int finishTime;
    private final int duration;
    private int processor = -1;
    private boolean isIdle = true; // a proper task by default

    /**
     * When a task is confirmed to be scheduled on a specific processor at a specific time, a data.Task object relating to
     * that task is instantiated. This is used in outputting the start time and processor number of each task to the
     * dot file.
     * @param id Unique Identification of the task
     * @param startTime The start time of the task on a processor
     * @param finishTime The finish time of the task on a processor
     * @param processor The processor the task is scheduled on (0 to P-1 where P is the number of processors)
     */
    public Task(int id, int startTime, int finishTime, int processor) {
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.duration = finishTime - startTime;
        this.processor = processor;
    }

    /**
     * Constructor for visual tasks.
     * @param startTime The start time of the time.
     * @param duration The duration of the task.
     * @param isIdle True if the slot does not contain a task.
     */
    public Task(int startTime, int duration, boolean isIdle) {
        this.startTime = startTime;
        this.duration = duration;
        this.finishTime = startTime + duration;
        this.isIdle = isIdle;
    }

    /**
     * @return The start time of the task.
     */
    public int getStartTime() { return startTime; }

    /**
     * @return The finish time of the task.
     */
    public int getFinishTime() { return finishTime; }

    /**
     * @return The duration of the task. This should be the same
     * as the weight of the task.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @return The processor that the task is scheduled on.
     * -1 if the task is not scheduled.
     */
    public int getProcessor() {
        return processor;
    }

    /**
     * Whether the task is an idle task or a proper task.
     * @return
     */
    public boolean isIdle() {
        return isIdle;
    }

    /**
     * Compares the start time of the current task and another task.
     * Used by a comparator.
     * @param otherTask The other task to compare to.
     * @return negative if this task should go before the other task, positive
     * if this task should go after the other task. 0 if the two tasks are equal.
     */
    @Override
    public int compareTo(Task otherTask) {
        return this.startTime - otherTask.startTime;
    }
}
