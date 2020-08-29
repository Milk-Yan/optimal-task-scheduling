package solution;

import java.util.LinkedList;

/**
 * The SolutionState class stores data which represents the current state of the search.
 */
public class SolutionState {

    LinkedList<Integer> candidateTasks; // Tasks that can currently be scheduled: they are unscheduled, and have no unscheduled parent tasks
    protected int[] inDegrees; // inDegrees[i] => number of unscheduled parent tasks of task i
    protected int[] taskStartTimes; // taskStartTimes[i] => start time of task i
    protected int[] scheduledOn;  // scheduledOn[i] => the processor task i is scheduled on
    protected int[] processorFinishTimes; // processorFinishTimes[i] => finishing time of the last task scheduled on processor i
    protected int remainingDuration = 0; // Sum of weights of unscheduled tasks

    public SolutionState(LinkedList<Integer> candidateTasks, int[] inDegrees, int[] taskStartTimes,
                         int[] scheduledOn, int[] processorFinishTimes, int remainingDuration) {
        this.candidateTasks = candidateTasks;
        this.inDegrees = inDegrees;
        this.taskStartTimes = taskStartTimes;
        this.scheduledOn = scheduledOn;
        this.processorFinishTimes = processorFinishTimes;
        this.remainingDuration = remainingDuration;
    }

     /**
     * Creates a deep copy of a SolutionState. This is used for multithreading.
     * @return SolutionState deep copy of this SolutionState instance.
     */
    public SolutionState getDeepCopy() {
        int n = inDegrees.length;
        int[] inDegreeDuplicate = new int[n];
        int[] taskStartTimesDuplicate = new int[n];
        int[] scheduledOnDuplicate = new int[n];
        int[] processorFinishTimesDuplicate = new int[processorFinishTimes.length];
        int remainingDurationDuplicate = remainingDuration;

        for(int i = 0; i < n; i++){
            inDegreeDuplicate[i] = inDegrees[i];
            taskStartTimesDuplicate[i] = taskStartTimes[i];
            scheduledOnDuplicate[i] = scheduledOn[i];
        }

        for(int i = 0; i < processorFinishTimes.length; i++){
            processorFinishTimesDuplicate[i] = processorFinishTimes[i];
        }
        LinkedList<Integer> candidateTasksDuplicate = new LinkedList<>(candidateTasks);
        SolutionState duplicate = new SolutionState(candidateTasksDuplicate, inDegreeDuplicate, taskStartTimesDuplicate,
                scheduledOnDuplicate, processorFinishTimesDuplicate, remainingDurationDuplicate);

        return duplicate;
    }
}
