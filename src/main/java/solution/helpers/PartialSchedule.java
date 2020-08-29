package solution.helpers;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class PartialSchedule {
    /**
     * This class contains a method which generates a hashcode that represents a partial schedule.
     * The hashcode is then used to check whether we have explored an equivalent partial schedule.
     * The hashcode is generated from the start times and which processor each task in scheduled on,
     * which means it will be unique for each schedule in the solution space that have different overall finish times.
     * @param startTimes startTimes[i] => start time of task i
     * @param scheduledOn scheduledOn[i] => the processor task i is scheduled on
     * @param numProcessors number of processors
     * @return hashcode representing partial solution
     */
    public static int generateHashCode(int[] startTimes, int[] scheduledOn, int numProcessors) {
        //Each stack represents a processor
        Set<Stack<Integer>> schedule = new HashSet<>();
        Stack<Integer>[] stacks = new Stack[numProcessors];

        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = new Stack<>();
        }

        //Add tasks ids and start times to the stack which represents the processor
        for(int i = 0; i < startTimes.length; i++){
            if(startTimes[i] != -1){
                stacks[scheduledOn[i]].add(i);
                stacks[scheduledOn[i]].add(startTimes[i]);
            }
        }

        // Add the stacks to a set.
        for(Stack<Integer> stack : stacks) {
            schedule.add(stack);
        }

        return schedule.hashCode();
    }
}