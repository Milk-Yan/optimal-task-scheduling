import java.util.List;

public class Driver {

    /**
     * Main method of the project from which everything is instantiated and run.
     * Instantiates an IOParser object which creates data structures from the inputs to the method. It then retrieves
     * these data structures using getter methods and passes them to a new Solution object.
     * Finally, it sends the result it retrieves from the Solution object to the IOParser object which then outputs the
     * solution in the required format.
     * @param args Array of string of inputs, in order: input file name, processor count, [OPTIONAL]: (-p) number of cores,
     *             (-v) visualisation of search, (-o) name of output file
     */
    public static void main(String[] args) {
        String fileName = args[0];
        int numProcessors = Integer.parseInt(args[1]);

        IOParser io = new IOParser(fileName);
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
