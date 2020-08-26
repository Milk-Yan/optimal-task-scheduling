import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PreProcessor {

    public static int[] maxLengthToExitNode(TaskGraph taskGraph){
        int numberOfTasks = taskGraph.getNumberOfTasks();
        int[] lengths = new int[numberOfTasks];

        for(int node = 0; node < numberOfTasks; node++){
            bLevels(node, lengths, taskGraph);
        }

        return lengths;
    }

    // Finds distances from exit node recursively with memoization
    private static int bLevels(int node, int[] lengths, TaskGraph taskGraph){
        if(lengths[node] != 0){
            return lengths[node];
        }

        List<Integer> childrenList  = taskGraph.getChildrenList(node);
        if(childrenList.isEmpty()){
            lengths[node] = taskGraph.getDuration(node);
            return lengths[node];
        }

        int maxLength = 0;
        for(int child : childrenList){
            maxLength = Math.max(maxLength, bLevels(child, lengths, taskGraph));
        }

        lengths[node] = maxLength + taskGraph.getDuration(node);
        return lengths[node];
    }


    public static ArrayList<Integer>[] getNodeEquivalence(TaskGraph taskGraph){
        HashSet<Integer> seen = new HashSet<>();
        int numTasks = taskGraph.getNumberOfTasks();
        ArrayList<Integer>[] equivalentNodesList = new ArrayList[numTasks];

        for(int i = 0; i<numTasks; i++) {
            if (!seen.contains(i)) {
                ArrayList<Integer> equivalentNodes = new ArrayList<>();
                equivalentNodes.add(i);
                for (int j = 0; j < numTasks; j++) {
                    if (j != i && !seen.contains(j)) {
                        boolean equivalent = compare(i, j, taskGraph);
                        if (equivalent) {
                            equivalentNodes.add(j);
                        }
                    }
                }
                for (int j = 0; j < equivalentNodes.size(); j++) {
                    equivalentNodesList[equivalentNodes.get(j)] = equivalentNodes;
                    seen.add(equivalentNodes.get(j));
                }
            }
        }
        return equivalentNodesList;
    }

    private static boolean compare(int a, int b, TaskGraph taskGraph){
        if(taskGraph.getDuration(a) != taskGraph.getDuration(b)) {
            return false;
        }

        List<Integer> aParents = taskGraph.getParentsList(a);
        List<Integer> bParents = taskGraph.getParentsList(b);
        List<Integer> aChildren = taskGraph.getChildrenList(a);;
        List<Integer> bChildren = taskGraph.getChildrenList(b);

        // the two tasks are only equal if they have the same parents and children.
        if((!new HashSet<>(aParents).equals(new HashSet<>(bParents)))
                || (!new HashSet<>(aChildren).equals(new HashSet<>(bChildren)))){
            return false;
        }
        return true;
    }


}
