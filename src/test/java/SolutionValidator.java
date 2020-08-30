import io.IOParser;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Utility class to check if a solution is valid given the constraints
 * of preferences.
 */
public class SolutionValidator {

    private int bestTime;

    /**
     * Finds if the input Task graph is a valid set of solutions.
    */
    public boolean validate(String inputFileName, String outputFileName, int numProcessors) {
        Graph inputGraph = IOParser.read(inputFileName);
        Graph outputGraph = IOParser.read(outputFileName);

        int numTasks = inputGraph.getNodeCount();
        if (numTasks != outputGraph.getNodeCount()) {
            System.out.println("solution size is " + outputGraph.getNodeCount() + ", but we should have " + numTasks + " tasks.");
            return false;
        } else if (numTasks == 0) {
            return true;
        }

        int[] inputDurations = new int[numTasks];
        int[][] commCosts = new int[numTasks][numTasks];

        int[] outputDurations = new int[numTasks];
        int[] startTimes = new int[numTasks];
        int[] scheduledOn = new int[numTasks];

        // the processors that we can check if it is occupied.
        Processor[] processors = new Processor[numProcessors];
        // initialise processors
        for (int i = 0; i < numProcessors; i++) {
            processors[i] = new Processor();
        }

        // get the input and output information
        for (int i = 0; i < numTasks; i++) {
            Node inputTask = inputGraph.getNode(i);
            Node outputTask = outputGraph.getNode(i);

            inputDurations[i] = ((Double)inputTask.getAttribute("Weight")).intValue();
            outputDurations[i] = ((Double)outputTask.getAttribute("Weight")).intValue();

            if (inputDurations[i] != outputDurations[i]) {
                System.out.println("The input and output graphs have different durations.");
                return false;
            }

            startTimes[i] = ((Double)outputTask.getAttribute("Start")).intValue();
            scheduledOn[i] = ((Double)outputTask.getAttribute("Processor")).intValue();

            outputTask.leavingEdges().forEach(e -> {
                int parent = e.getSourceNode().getIndex();
                int child = e.getTargetNode().getIndex();
                int commCost = ((Double)e.getAttribute("Weight")).intValue();

                commCosts[parent][child] = commCost;
            });

            // check if a processor gets occupied at the same time, which is invalid
            if (processors[scheduledOn[i]-1].isOccupied(startTimes[i], inputDurations[i])) {
                System.out.println("data.Task that is occupied already during this time frame is " +
                        " getting used.");
                return false;
            } else {
                processors[scheduledOn[i]-1].add(startTimes[i], inputDurations[i]);
            }
        }

        // check if dependencies are respected in the output graph.
        for (int i = 0; i < numTasks; i++) {
            if (!parentsCompleteBeforeTask(startTimes, inputDurations, commCosts, numTasks, scheduledOn)) {
                System.out.println("Parents are not complete before task.");
                return false;
            }
            bestTime = Math.max(bestTime, startTimes[i] + outputDurations[i]);
        }

        return true;
    }

    /**
     * Find if all the parents of the task (all the nodes are ingoing edges
     * to the task) are complete before the task. Also counts for the difference
     * in transfer time in between processors.
     * @return Whether the parents have been completed before the task
     */
    private boolean parentsCompleteBeforeTask(int[] startTimes, int[] durations, int[][] commCosts, int numTasks,
                                              int[] scheduledOn) {
        for (int parent = 0; parent < numTasks; parent++) {
            for (int child = 0; child < numTasks; child++) {
                if (commCosts[parent][child] != 0) {
                    int commCost = 0;
                    if (scheduledOn[parent] != scheduledOn[child]) {
                        commCost = commCosts[parent][child];
                    }

                    if ((startTimes[parent] + durations[parent] + commCost) > startTimes[child]) {
                        System.out.println("child started before parent could finish");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * @return The best finish time of the validated solution.
     */
    public int getBestTime() {
        return bestTime;
    }
}
