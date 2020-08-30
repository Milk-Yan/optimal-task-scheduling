package solution.helpers;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * The PartialScheduleHashGenerator class encapsulates the logic behind creating a unique hashcode to represent partial schedules.
 *
 * The hashcode of partial schedules are added to a set, so we can check whether we have already visited an equivalent state.
 * This allows us to prune the equivalent states as we can immediately return when the hashcode of a state we are exploring
 * matches a hashcode already in the set.
 */
public class PartialScheduleHashGenerator {

     /**
     * Generates a hashcode that represents a partial schedule.
     * The hashcode is used to check whether we have explored an equivalent partial schedule.
     * The hashcode is generated from the start times and which processor each task in scheduled on,
     * which means it will be unique for each schedule in the solution space that have different overall finish times.
     * @param startTimes startTimes[i] => start time of task i
     * @param scheduledOn scheduledOn[i] => the processor task i is scheduled on
     * @param numProcessors number of processors
     * @return hashcode representing partial solution
     */
    public static HashSet<Integer> generateHashCode(int[] startTimes, int[] scheduledOn, int numProcessors) {
        //Each stack represents a processor
        HashSet<Integer> schedule = new HashSet<>();
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
            schedule.add(stack.hashCode());
        }

        return schedule;
    }
}