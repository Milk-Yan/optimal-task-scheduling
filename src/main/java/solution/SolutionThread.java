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
public class SolutionThread extends Thread {
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
    public SolutionThread(Solution solution, TaskGraph taskGraph, int numProcessors, String outputFilePath, Graph dotGraph) {
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

        // if the number of processors is one, then the optimal solution is just everything run
        // sequentially.
        if (numProcessors == 1) {
            SequentialScheduler scheduler = new SequentialScheduler(taskGraph);
            result = scheduler.getSchedule();
            solution.setInitialSchedule(result);
            solution.setDone();
        } else {
            // Run greedy algorithm to determine lower bound of optimal solution
            Greedy g = new Greedy();
            result = g.run(taskGraph, numProcessors);
            solution.setInitialSchedule(result);

            // Run algorithm to find optimal schedule
            long startTime = System.currentTimeMillis();
            Schedule optimalResult = solution.run();

            if (optimalResult.getFinishTime() < result.getFinishTime()) {
                result = optimalResult;
            }

            System.out.println("Program ran in: " + (System.currentTimeMillis() - startTime) + "ms");
            System.out.println("Best schedule has finishing time of " + result.getFinishTime());
        }

        IOParser.write(outputFilePath, dotGraph, result);
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
