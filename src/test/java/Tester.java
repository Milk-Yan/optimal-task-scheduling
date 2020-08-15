import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Tester {

    ValidateSolution validater = new ValidateSolution();

    @Test
    public void test() {

        /**
         * A -> B -> C
         */
        final int n = 3;
        ArrayList<Integer>[] inList = (ArrayList<Integer>[]) new ArrayList[n];
        inList[1] = new ArrayList<>();
        inList[1].add(0);
        inList[2] = new ArrayList<>();
        inList[2].add(1);

        ArrayList<Integer>[] outList = (ArrayList<Integer>[]) new ArrayList[n];
        outList[0] = new ArrayList<>();
        outList[0].add(1);
        outList[1] = new ArrayList<>();
        outList[1].add(2);

        /**
         * cost of A -> B: 2
         * cost of A -> C: 3
         * cost of B -> A: 5
         * cost of B -> C: 2
         * cost of C -> A: 5
         * cost of C -> B: 1
         */
        int[][] commCosts = new int[n][n];
        commCosts[0][1] = 2;
        commCosts[0][2] = 3;
        commCosts[1][0] = 5;
        commCosts[1][2] = 2;
        commCosts[2][0] = 5;
        commCosts[2][1] = 1;

        int[] durations = new int[n];
        durations[0] = 2;
        durations[1] = 2;
        durations[2] = 2;

        int numProcessors = 2;

        Solution solution = new Solution();
        Task[] result = solution.run(inList, outList, commCosts, durations, numProcessors);

        assertTrue(validater.validate(inList, commCosts, durations, numProcessors, result));
    }

    @Test
    public void testEmpty() {
        assertEquals(true, validater.validate(null, null, null, 2, null));
    }
}
