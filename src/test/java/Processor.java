package test.java;

import main.java.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for each processor that checks for overlaps.
 */
public class Processor {

    List<Integer> startTimes = new ArrayList<>();
    List<Integer> endTimes = new ArrayList<>();

    public boolean isOccupied(Task task) {

        for (int i = 0; i < startTimes.size(); i++) {
            int startTime = startTimes.get(i);
            int endTime = endTimes.get(i);

            if (task.getStartTime() < endTime && task.getFinishTime() > startTime) {
                return true;
            }
        }

        return false;
    }

    public void add(Task task) {
        startTimes.add(task.getStartTime());
        endTimes.add(task.getFinishTime());
    }

}
