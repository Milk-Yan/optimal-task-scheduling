import com.sun.javafx.application.PlatformImpl;
import data.Schedule;
import data.TaskGraph;
import gui.Visualiser;
import io.IOParser;
import javafx.stage.Stage;
import org.apache.commons.cli.*;
import org.graphstream.graph.Graph;

import solution.Solution;
import solution.SolutionParallel;
import solution.SolutionSequential;
import solution.SolutionThread;
import solution.helpers.Greedy;
import solution.helpers.SequentialScheduler;

/**
 * The main class of the project. Runs different options of solutions for the task
 * scheduling problem depending on inputs.
 */
public class Driver {
    static int numProcessors;
    static int numThreads = 1;
    static String fileName;
    static TaskGraph taskGraph;

    /**
     * Main method of the project from which everything is instantiated and run.
     * Uses the IOParser class to create a TaskGraph object. A solution object uses
     * the TaskGraph object to create a schedule represented by an array of Tasks.
     * We use the IOParser to write the schedule to the output dot file.
     * @param args Array of string of inputs, in order: input file name, processor count,
     *             [OPTIONAL]: (-p) number of cores, (-v) visualisation of search,
     *             (-o) name of output file
     */
    public static void main(String[] args){
        // Get the input options from the command line
        CommandLine cmd = getCommandLineOptions(args);
        fileName = args[0];

        numProcessors = getNumProcessors(args);
        final String outputFilePath = getOutputFilePath(cmd);

        // Read input file
        Graph dotGraph = IOParser.read(fileName);
        taskGraph = new TaskGraph(dotGraph);

        // Choose to run either the sequential or the parallel version.
        Solution solution;
        if(cmd.hasOption("p")){
            solution = new SolutionParallel(taskGraph, numProcessors);
            try {
                numThreads = Integer.parseInt(cmd.getOptionValue('p'));
                ((SolutionParallel) solution).setNumCores(numThreads);
            } catch(NumberFormatException e) {
                System.err.println("Error: number of threads invalid");
                System.exit(1);
            }
        } else {
            solution = new SolutionSequential(taskGraph, numProcessors);
        }

        // Choose whether to run visualisation.
        if(cmd.hasOption('v')) {
            runVisual(solution, outputFilePath, dotGraph);
        } else {
            runNonVisual(solution, outputFilePath, dotGraph);
        }
    }

    /**
     * Makes a CommandLine with the below options:
     * -p: number of cores/threads.
     * -v: if visualisation is required.
     * -o: name of output file.
     * @param args The command line arguments.
     * @return A CommandLine object containing the results of the command line
     * arguments.
     */
    private static CommandLine getCommandLineOptions(String[] args){
        Options options = new Options();
        Option p = new Option("p", true, "number of cores");
        p.setRequired(false);
        options.addOption(p);

        Option v = new Option("v", false, "visualization");
        v.setRequired(false);
        options.addOption(v);

        Option o = new Option("o", true, "output");
        o.setRequired(false);
        options.addOption(o);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("There was an issue reading the command line inputs");
            System.err.println("Please ensure that the program is run like: java -jar scheduler.jar INPUT.dot P [OPTION]");
            System.exit(1);
        }

        return cmd;
    }

    /**
     * @param args The command line arguments.
     * @return The number of processors to use.
     */
    private static int getNumProcessors(String[] args) {
        // Get num processors for schedule
        try {
            return Integer.parseInt(args[1]);
        } catch(NumberFormatException e){
            System.err.println("Error: number of processors invalid");
            System.exit(1);
        }

        return -1;
    }

    /**
     * @param cmd The CommandLine object that contains input arguments.
     * @return The output file path to write to.
     */
    private static String getOutputFilePath(CommandLine cmd) {
        String outputFilePath = cmd.getOptionValue('o',
                fileName.split("\\.")[0] + "-output.dot");
        if (!outputFilePath.endsWith(".dot")) {
            outputFilePath += ".dot";
        }

        return outputFilePath;
    }

    /**
     * Runs the visualisation of the project.
     * @param solution The solution to visualise.
     * @param outputFilePath The path to write the output file to.
     * @param dotGraph The input graph.
     */
    private static void runVisual(Solution solution, String outputFilePath, Graph dotGraph) {
        PlatformImpl.startup(() -> {
            Visualiser visualiser = new Visualiser();
            SolutionThread solutionThread = new SolutionThread(solution, taskGraph, numProcessors, outputFilePath, dotGraph);
            try {
                visualiser.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            visualiser.setUpArgs(solutionThread, numProcessors, fileName, taskGraph.getNumberOfTasks(), numThreads);
        });
    }

    /**
     * Runs the project without visualisation.
     * @param solution The solution to run.
     * @param outputFilePath The path to write the output file to.
     * @param dotGraph The input graph.
     */
    private static void runNonVisual(Solution solution, String outputFilePath, Graph dotGraph) {
        Schedule result;

        // if the number of processors is one, then the optimal solution is just everything run
        // sequentially.
        if (numProcessors == 1) {
            SequentialScheduler scheduler = new SequentialScheduler(taskGraph);
            result = scheduler.getSchedule();
            solution.setInitialSchedule(result);
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
}
