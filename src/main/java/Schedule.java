public class Schedule {
    private Task[] tasks;
    private int finishTime;

    public Schedule(Task[] tasks, int finishTime) {
        this.tasks = tasks;
        this.finishTime = finishTime;
    }

    public Task[] getTasks() {
        return tasks;
    }

    public int getFinishTime() {
        return finishTime;
    }
}
