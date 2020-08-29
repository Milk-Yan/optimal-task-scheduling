import org.apache.commons.cli.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDOT;

import java.io.IOException;
import java.util.Random;

/**
 * Generates x random graphs of sizes MIN_SIZE-MAX_SIZE inclusive.
 * X is a command line parameter that will be passed in.
 */
public class RandomDotGraphGenerator {

    private static int MIN_SIZE = 0;
    private static int MAX_SIZE = 20;
    private static int MAX_TASK_WEIGHT = 100;
    private static int MAX_EDGE_WEIGHT = 100;

    public static void main(String[] args) {
        try {
            int numGraphs = Integer.parseInt(args[0]);
            CommandLine cmd = getCommandLineOptions(args);
            initializeFieldsAccordingToCmd(cmd);

            for (int i = 0; i < numGraphs; i++) {
                generateRandomGraph(i);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter the number of random graphs to generate.");
            System.exit(-1);
        }

    }

    /**
     * Generate a single random graph of size MIN_SIZE-MAX_SIZE inclusive.
     */
    private static void generateRandomGraph(int graphNum) {
        Random randomGenerator = new Random();

        int numTasks = MIN_SIZE + randomGenerator.nextInt(MAX_SIZE-MIN_SIZE+1);
        int numEdges = 0;
        int[] weights = new int[numTasks];
        int[][] edges = new int[numTasks][numTasks];

        for (int i = 0; i < numTasks; i++) {
            // randomly assign weights
            weights[i] = 1 + randomGenerator.nextInt(MAX_TASK_WEIGHT);
            for (int j = i+1; j < numTasks; j++) {
                // randomly assign edges
                // randomly determine whether to have an edge from i to j
                if (randomGenerator.nextBoolean()) {
                    // randomly determine the edge's weight
                    int edgeWeight = randomGenerator.nextInt(MAX_EDGE_WEIGHT + 1);
                    edges[i][j] = edgeWeight;
                    numEdges++;
                }
            }
        }

        String outputFileName = "RandomGraph" + graphNum + "_" + numTasks + "Tasks" + numEdges + "Edges.dot";
        write(outputFileName, numTasks, weights, numEdges, edges);
    }

    /**
     * Writes a generated random graph to an output dot file.
     */
    private static void write(String outputFileName, int numTasks, int[] taskWeights, int numEdges, int[][] edgeWeights) {
        Graph dotGraph = new DefaultGraph(outputFileName);

        for (int i = 0; i < numTasks; i++) {
            Node node = dotGraph.addNode(String.valueOf(i));
            node.setAttribute("Weight", taskWeights[i]);
        }

        for (int parent = 0; parent < numTasks; parent++) {
            for (int child = 0; child < numTasks; child++) {
                if (edgeWeights[parent][child] != 0) {
                    String label = parent + "" + child;
                    Edge edge = dotGraph.addEdge(label, parent, child, true);
                    edge.setAttribute("Weight", edgeWeights[parent][child]);
                }
            }
        }

        FileSink file = new FileSinkDOT(true);
        try {
            file.writeAll(dotGraph, outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CommandLine getCommandLineOptions(String[] args){
        Options options = new Options();
        Option maxSize = new Option("maxSize", true, "maximum number of tasks");
        maxSize.setRequired(false);
        options.addOption("maxSize", true, "maximum number of tasks");

        Option minSize = new Option("minSize", false, "minimum number of tasks");
        minSize.setRequired(false);
        options.addOption("minSize", true, "minimum number of tasks");

        Option maxTaskWeight = new Option("maxTaskWeight", true, "maximum weight of tasks");
        maxTaskWeight.setRequired(false);
        options.addOption("maxTaskWeight", true, "maximum weight of tasks");

        Option maxEdgeWeight = new Option("maxEdgeWeight", true, "maximum weight of edges");
        maxEdgeWeight.setRequired(false);
        options.addOption("maxEdgeWeight", true, "maximum weight of edges");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("There was an issue reading the command line inputs");
            System.exit(1);
        }

        return cmd;
    }

    private static void initializeFieldsAccordingToCmd(CommandLine cmd) {
        try {
            if (cmd.hasOption("maxSize")) {
                MAX_SIZE = Integer.parseInt(cmd.getOptionValue("maxSize"));
            }

            if (cmd.hasOption("minSize")) {
                System.out.println(cmd.getOptionValue("minSize"));
                MIN_SIZE = Integer.parseInt(cmd.getOptionValue("minSize"));
            }

            if (cmd.hasOption("maxTaskWeight")) {
                MAX_TASK_WEIGHT = Integer.parseInt(cmd.getOptionValue("maxTaskWeight"));
            }

            if (cmd.hasOption("maxEdgeWeight")) {
                MAX_EDGE_WEIGHT = Integer.parseInt(cmd.getOptionValue("maxEdgeWeight"));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

}
