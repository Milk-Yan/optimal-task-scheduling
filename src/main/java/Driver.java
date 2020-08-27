import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.cli.*;
import org.graphstream.graph.Graph;

import java.io.IOException;

public class Driver extends Application {
    static int numProcessors;
    static int numThreads = 1;
    static String fileName;
    static TaskGraph taskGraph;

    /**
     * Main method of the project from which everything is instantiated and run.
     * Uses the IOParser class to create a TaskGraph object. A solution object uses the TaskGraph object to create a schedule represented by an array of Tasks.
     * We use the IOParser to write the schedule to the output dot file.
     * @param args Array of string of inputs, in order: input file name, processor count, [OPTIONAL]: (-p) number of cores,
     *             (-v) visualisation of search, (-o) name of output file
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CommandLine cmd = getCommandLineOptions(args);
        fileName = args[0];
        String outputFilePath = cmd.getOptionValue('o', fileName.split("\\.")[0] + "-output.dot");
        if (!outputFilePath.endsWith(".dot")) {
            outputFilePath = outputFilePath.concat(".dot");
        }

        numProcessors = 1; // Default value

        // Get num processors for schedule
        try {
            numProcessors = Integer.parseInt(args[1]);
        } catch(NumberFormatException e){
            System.err.println("Error: number of processors invalid");
            System.exit(1);
        }

        // Get num threads to run program on
        Solution solution;
        if(cmd.hasOption("p")){
            solution = new SolutionParallel();
            try {
                numThreads = Integer.parseInt(cmd.getOptionValue('p'));
            } catch(NumberFormatException e) {
                System.err.println("Error: number of threads invalid");
                System.exit(1);
            }
        } else {
            solution = new SolutionSequential();
        }

        // Read input file
        Graph dotGraph = IOParser.read(fileName);
        taskGraph = new TaskGraph(dotGraph);


        // Get whether the user wants visualisation
        if(cmd.hasOption('v')){
            launch(args);
        }

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
            System.out.println(System.currentTimeMillis() - startTime);
        }



        // Our solution ignores all schedules that are >= than the greedy schedule,
        // so this is to ensure if nothing is faster, we return the greedy schedule.
        if (greedySchedule != null && result.getFinishTime() >= greedySchedule.getFinishTime()) {
            IOParser.write(outputFilePath, dotGraph, greedySchedule.getTasks());
        } else {
            IOParser.write(outputFilePath, dotGraph, result.getTasks());
        }
        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader  = new FXMLLoader(getClass().getResource("visualisation-view.fxml"));
        Parent root = loader.load();

        Controller controller = loader.getController();
        controller.setUpArgs(numProcessors, fileName, taskGraph.getNumberOfTasks(), numThreads);
        controller.addTask(0,100,10);
        controller.addTask(1,200,10);
        controller.removeLast();
        controller.addTask(2,100,100);
        controller.addTask(0,100,150);

        controller.incrementState();
        controller.incrementState();

        controller.setBestFinishTime(200);

        primaryStage.setTitle("Task Scheduler Visualisation");
        primaryStage.setScene(new Scene(root, 780, 525));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("logo.png"));
        primaryStage.show();

        controller.addTask(0,100,260);
        controller.addTask(1,100,100);
        controller.removeLast();
        controller.addTask(3,100,100);
        controller.addTask(1,1000,500);
        controller.addTask(1,130,1500);
    }

    private static CommandLine getCommandLineOptions(String[] args){
        Options options = new Options();
        Option p = new Option("p", true, "numCores");
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
}
