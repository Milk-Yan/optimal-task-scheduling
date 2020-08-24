import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.*;

public class Driver extends Application {

    /**
     * Main method of the project from which everything is instantiated and run.
     * Uses the IOParser class to create a TaskGraph object. A solution object uses the TaskGraph object to create a schedule represented by an array of Tasks.
     * We use the IOParser to write the schedule to the output dot file.
     * @param args Array of string of inputs, in order: input file name, processor count, [OPTIONAL]: (-p) number of cores,
     *             (-v) visualisation of search, (-o) name of output file
     */
    public static void main(String[] args) {
        CommandLine cmd = getCommandLineOptions(args);
        String fileName = args[0];
        String outputFilePath = cmd.getOptionValue('o', fileName.split("\\.")[0] + "-output.dot");
        if (!outputFilePath.endsWith(".dot")) {
            outputFilePath = outputFilePath.concat(".dot");
        }

        int numProcessors = 1; // Default value
        int numThreads = 1; // Default value

        // Get num processors for schedule
        try {
            numProcessors = Integer.parseInt(args[1]);
        } catch(NumberFormatException e){
            System.err.println("Error: number of processors invalid");
            System.exit(1);
        }

        // Get num threads to run program on
        if(cmd.hasOption("p")){
            try {
                numThreads = Integer.parseInt(cmd.getOptionValue('p'));
            } catch(NumberFormatException e) {
                System.err.println("Error: number of threads invalid");
                System.exit(1);
            }
            System.err.println("Note: the parallelised version has not been implemented yet, program will run on one thread");
        }

        // Get whether the user wants visualisation
        if(cmd.hasOption('v')){
            launch(args);
        }

        // Read input file
        TaskGraph taskGraph = IOParser.read(fileName);

        // Run greedy algorithm to determine lower bound of optimal solution
        Greedy g = new Greedy();
        int greedyTime = g.run(taskGraph, numProcessors);

        // Run algorithm to find optimal schedule
        Solution solution = new Solution();
        Task[] result = solution.run(taskGraph, numProcessors, greedyTime);

        IOParser.write(outputFilePath, taskGraph, result);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("here");
        FXMLLoader loader  = new FXMLLoader(getClass().getResource("visualisation-view.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Task Scheduler Visualisation");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

    }

    private static CommandLine getCommandLineOptions(String[] args){
        Options options = new Options();
        Option p = new Option("p", true, "numCores");
        p.setRequired(false);
        Option v = new Option("v", false, "visualization");
        v.setRequired(false);
        Option o = new Option("o", true, "output");
        o.setRequired(false);
        options.addOption(p);
        options.addOption(v);
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
