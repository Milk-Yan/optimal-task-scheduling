package main;

public class Task {
    int id;
    int startTime;
    int finishTime;
    int processor;

    public Task(int id, int startTime, int finishTime, int processor) {
        this.id = id;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.processor = processor;
    }
}
