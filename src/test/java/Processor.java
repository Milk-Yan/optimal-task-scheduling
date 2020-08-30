import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for each processor that checks for overlaps.
 */
public class Processor {

    List<Integer> startTimes = new ArrayList<>();
    List<Integer> endTimes = new ArrayList<>();

    /**
     * @param startTime The start time of the task.
     * @param duration The duration of the task.
     * @return Whether the processor is occupied during the duration of the task.
     */
    public boolean isOccupied(int startTime, int duration) {

        int finishTime = startTime + duration;

        for (int i = 0; i < startTimes.size(); i++) {
            int occupiedStartTime = startTimes.get(i);
            int occupiedEndTime = endTimes.get(i);

            if (startTime < occupiedEndTime && finishTime > occupiedStartTime) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds the duration of the task to the processor so it shows as occupied during
     * that time.
     * @param startTime The start time of the processor.
     * @param duration The duration of the processor.
     */
    public void add(int startTime, int duration) {
        startTimes.add(startTime);
        endTimes.add(startTime + duration);
    }

}
