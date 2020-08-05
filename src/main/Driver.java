package main;

import java.util.List;

public class Driver {

    public static void main(String[] args) {
        String fileName = args[0];
        int numProcessors = Integer.parseInt(args[1]);

        IOParser io = new IOParser();
        io.read(fileName);
        List<int[]> inList = io.getInList(fileName);
        List<int[]> outList = io.getOutList(fileName);

        Solution solution = new Solution();
        int[][] result = solution.run(inList, outList, numProcessors);

        io.write(fileName, result);
    }
}
