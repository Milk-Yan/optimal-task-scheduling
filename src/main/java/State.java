import java.io.*;
import java.util.LinkedList;

public class State implements Serializable {
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

    public State getDeepCopy() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());
        return (State) new ObjectInputStream(bais).readObject();
    }



}
