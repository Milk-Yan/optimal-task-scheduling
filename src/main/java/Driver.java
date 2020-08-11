package main.java;

import java.util.List;

public class Driver {

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
