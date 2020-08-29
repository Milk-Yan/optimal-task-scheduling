package solution;

import java.util.LinkedList;

public class State {

    LinkedList<Integer> candidateTasks; // list of free tasks
    protected int[] inDegrees; // inDegrees[i] => number of unscheduled parent tasks of task i
    protected int[] taskStartTimes; // taskStartTimes[i] => start time of task i
    protected int[] scheduledOn;  // scheduledOn[i] => the processor task i is scheduled on
    protected int[] processorFinishTimes; // processorFinishTimes[i] => finishing time of the last task scheduled on processor i
    protected int remainingDuration = 0; // total duration of remaining tasks to be scheduled (used for pruning)\

    public State(LinkedList<Integer> candidateTasks, int[] inDegrees, int[] taskStartTimes,
                 int[] scheduledOn, int[] processorFinishTimes, int remainingDuration) {
        this.candidateTasks = candidateTasks;
        this.inDegrees = inDegrees;
        this.taskStartTimes = taskStartTimes;
        this.scheduledOn = scheduledOn;
        this.processorFinishTimes = processorFinishTimes;
        this.remainingDuration = remainingDuration;
    }

    public State getDeepCopy() {
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
        State duplicate = new State(candidateTasksDuplicate, inDegreeDuplicate, taskStartTimesDuplicate,
                scheduledOnDuplicate, processorFinishTimesDuplicate, remainingDurationDuplicate);

        return duplicate;
    }

}
