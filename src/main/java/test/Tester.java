package test;

import main.Task;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class Tester{

    ValidateSolution validater = new ValidateSolution();
    @Test
    public void test() {

        /**
         * A -> B -> C
         */
        final int n = 3;
        ArrayList<Integer>[] inList = (ArrayList<Integer>[]) new ArrayList[n];
        inList[1] = new ArrayList<Integer>();
        inList[1].add(0);
        inList[2] = new ArrayList<>();
        inList[2].add(1);

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

        Task[] solution = new Task[n];
        Task taskA = new Task(0, 0, 2, 0);
        Task taskB = new Task(1, 2, 4, 0);
        Task taskC = new Task(2, 4, 6, 0);
        solution[0] = taskA;
        solution[1] = taskB;
        solution[2] = taskC;

        assertEquals(true, validater.validate(inList, commCosts, durations, numProcessors, solution));
    }

    @Test
    public void testEmpty() {
        assertEquals(true, validater.validate(null, null, null, 2, null));
    }
}
