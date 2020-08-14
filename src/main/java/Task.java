import java.util.ArrayList;
import java.util.List;

/**
 * Class to keep track of all information related to a scheduled task
 */
public class Task {
    int id;
    int startTime;
    int finishTime;
    int processor;
    List<Task> parents = new ArrayList<>();

    /**
     * When a task is confirmed to be scheduled on a specific processor at a specific time, a Task object relating to
     * that task is instantiated. This is used in outputting the start time and processor number of each task to the
     * dot file.
     *
     * @param id Unique Identification of the task
     * @param startTime The start time of the task on a processor
     * @param finishTime The finish time of the task on a processor
     * @param processor The processor the task is scheduled on (0 to P-1 where P is the number of processors)
     */
    public Task(int id, int startTime, int finishTime, int processor) {
        this.id = id;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.processor = processor;
    }

    public int getProcessor() {
        return processor;
    }

    public int getStartTime() { return startTime; }

    public int getFinishTime() { return finishTime; }

    public int getId() { return id; }

    // Testing purposes
    public void addParent(Task parent) {
        parents.add(parent);
    }

    public List<Task> getParents() {
        return parents;
    }
}
