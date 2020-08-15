import java.util.List;
import org.apache.commons.cli.*;

public class Driver {

    /**
     * Main method of the project from which everything is instantiated and run.
     * Instantiates an IOParser object which creates data structures from the inputs to the method. It then retrieves
     * these data structures using getter methods and passes them to a new Solution object.
     * Finally, it sends the result it retrieves from the Solution object to the IOParser object which then outputs the
     * solution in the required format.
     * This class uses an external library from Apache Commons.
     * @param args Array of string of inputs, in order: input file name, processor count, [OPTIONAL]: (-p) number of cores,
     *             (-v) visualisation of search, (-o) name of output file
     */
    public static void main(String[] args) {
        String fileName = args[0];
        int numProcessors = 1;
        try {
            //If the num processors is not valid, i.e has not be given, then a number-
            //format exception would be thrown, and the program will exit.
            numProcessors = Integer.parseInt(args[1]);
        } catch(NumberFormatException e){
            System.out.println("Error number of processors invalid: Please enter the number of processors available");
            System.exit(1);
        }
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
        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        //Default number of threads to use is 1;
        int threads = 1;
        boolean visual = false;
        String outputFilePath = args[0] + "-output.dot";
        try {
            cmd = parser.parse(options, args);
            //If p is set, then get the value;
            if(cmd.hasOption("p")){
                threads = Integer.parseInt(cmd.getOptionValue('p'));
                System.out.println("Note: the parallelised version has not been implemented yet");
            }
            outputFilePath = cmd.getOptionValue('o', args[0] + "-output.dot");
            boolean visual = cmd.hasOption('v');
            if(visual){
                System.out.println("Note: the visual version has not been implemented yet");
            }
        } catch (ParseException e) {
            System.out.println("There was an issue reading the command line inputs");
            System.out.println("Please insure that the program is run like: java -jar scheduler.jar INPUT.dot P [OPTION]");
            System.exit(1);
        }
        IOParser io = new IOParser(fileName, outputFilePath);
        io.read();
        List<Integer>[] inList = io.getInList();
        List<Integer>[] outList = io.getOutList();
        int[][] commCosts = io.getCommCosts();
        int[] durations = io.getDurations();
        Solution solution = new Solution();
        Task[] result = solution.run(inList, outList, commCosts, durations, numProcessors);
        io.write(result);
    }
}
