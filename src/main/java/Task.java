import java.util.ArrayList;
import java.util.List;

public class Task {
    int id;
    int startTime;
    int finishTime;
    int processor;
    List<Task> parents = new ArrayList<>();

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

    public void addParent(Task parent) {
        parents.add(parent);
    }

    public List<Task> getParents() {
        return parents;
    }
}
