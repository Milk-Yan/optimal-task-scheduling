import java.lang.reflect.Array;
import java.util.*;

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
        if((aParents.size() != bParents.size()) || (aChildren.size() != bChildren.size())){
            return false;
        }

        int numTasks = taskGraph.getNumberOfTasks();
        int[][] parentsAdjacencyMatrix = taskGraph.getParentsAdjacencyMatrix();
        int[][] childrenAdjacencyMatrix = taskGraph.getChildrenAdjacencyMatrix();
        for(int i = 0; i < numTasks; i++){
            if(parentsAdjacencyMatrix[a][i] != parentsAdjacencyMatrix[b][i] || childrenAdjacencyMatrix[a][i] != childrenAdjacencyMatrix[b][i]){
                return false;
            }
        }

        return true;
    }


}
