package solution;

import data.Schedule;
import data.Task;
import data.TaskGraph;
import io.IOParser;
import org.graphstream.graph.Graph;
import solution.helpers.Greedy;
import solution.helpers.SequentialScheduler;

import java.util.List;

/**
 * This class acts as a wrapper for a Solution object so that it can publish information for the GUI to use.
 * This is done by extending the thread class, so that it may be polled.
 * The intermediate results of the solution are accessed by a poller via getters provided in this class.
 */
public class VisualThread extends Thread {
    private final Solution solution;
    private final TaskGraph taskGraph;
    private final int numProcessors;
    private final String outputFilePath;
    private final Graph dotGraph;

    /**
     * @param solution The solution that runs on this thread.
     * @param taskGraph The input graph on which the solution runs.
     * @param numProcessors The number of processors to schedule tasks on.
     * @param outputFilePath The path to the output file.
     * @param dotGraph The input graph in dot format.
     */
    public VisualThread(Solution solution, TaskGraph taskGraph, int numProcessors, String outputFilePath, Graph dotGraph) {
        super();
        this.solution = solution;
        solution.setVisual(); // flag the solution as visual
        this.taskGraph = taskGraph;
        this.numProcessors = numProcessors;
        this.outputFilePath = outputFilePath;
        this.dotGraph = dotGraph;
    }

    public void run() {
        Schedule result;
        Schedule greedySchedule = null;

        // if the number of processors is one, then the optimal solution is just everything run
        // sequentially.
        if (numProcessors == 1) {
            SequentialScheduler scheduler = new SequentialScheduler(taskGraph);
            result = scheduler.getSchedule();
        } else {
            // Run greedy algorithm to determine lower bound of optimal solution
            Greedy g = new Greedy();
            greedySchedule = g.run(taskGraph, numProcessors);

            // Run algorithm to find optimal schedule
            long startTime = System.currentTimeMillis();
            result = solution.run(taskGraph, numProcessors, greedySchedule.getFinishTime());
            System.out.println("Program ran in: " + (System.currentTimeMillis() - startTime) + "ms");
        }

        // Our solution ignores all schedules that are >= than the greedy schedule,
        // so this is to ensure if nothing is faster, we return the greedy schedule.
        if (greedySchedule != null && result.getFinishTime() >= greedySchedule.getFinishTime()) {
            IOParser.write(outputFilePath, dotGraph, greedySchedule.getTasks());
        } else {
            IOParser.write(outputFilePath, dotGraph, result.getTasks());
        }
    }

    // Getter methods by which the poller of this thread can access the published results of the solution.

    public synchronized int getCurrentBest() {
        return solution.bestFinishTime;
    }

    public synchronized long getStateCount() {
        return solution.stateCount;
    }

    public synchronized boolean isDone() {
        return solution.isDone;
    }

    public synchronized List<Task>[] getBestSchedule() {
        return solution.bestSchedule;
    }

    // Check that the best solution has changed since last received.
    public synchronized boolean getBestChanged() {
        boolean hasChanged = solution.bestChanged;
        solution.bestChanged = false;
        return hasChanged;
    }
}
