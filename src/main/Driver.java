package main;

import java.util.List;

public class Driver {

    public static void main(String[] args) {
        String fileName = args[0];
        int numProcessors = Integer.parseInt(args[1]);

        IOParser io = new IOParser();
        List<int[]> inputs = io.read(fileName);

        Solution solution = new Solution();
        int[][] result = solution.run(inputs, numProcessors);

        io.write(fileName, result);
    }
}
