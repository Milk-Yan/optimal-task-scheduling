import java.util.List;
import org.apache.commons.cli.*;

public class Driver {

    public static void main(String[] args) {
        String fileName = args[0];
        int numProcessors = Integer.parseInt(args[1]);

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

        try {
            cmd = parser.parse(options, args);
            int parallel = Integer.parseInt(cmd.getOptionValue('p'));
            String outputFilePath = cmd.getOptionValue('o', "INPUT-output");
            boolean visual = cmd.hasOption('v');
            ///
            System.out.println(parallel);
            System.out.println(outputFilePath);
            System.out.println(visual);
            ///
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

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
