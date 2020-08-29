package solution.helpers;

import data.TaskGraph;

import java.util.*;

/**
 * This class calculates information needed for pruning and optimization in the Solution classes.
 * It contains methods for calculating B-Levels of nodes and node equivalence.
 */
public class PreProcessor {


    /**
     * This method is called by a client to calculate the B-levels of the nodes in the DAG
     *
     * @param taskGraph The task graph contains information about the DAG
     * @return an array of ints where index i is the B-Level of node i.
     */
    public static int[] maxLengthToExitNode(TaskGraph taskGraph){
        int numberOfTasks = taskGraph.getNumberOfTasks();
        int[] lengths = new int[numberOfTasks];

        //For every node in the graph, call b-levels.
        for(int node = 0; node < numberOfTasks; node++){
            bLevels(node, lengths, taskGraph);
        }

        return lengths;
    }


    /**
     * This method finds the B-Level of a node.
     *
     * @param node the node that we want to find the B-Level for.
     * @param lengths the memoization table with the found B-Levels
     * @param taskGraph an object that contains information about the DAG
     * @return
     */
    private static int bLevels(int node, int[] lengths, TaskGraph taskGraph){
        //If the b-level of this node has already been calculated return it.
        if(lengths[node] != 0){
            return lengths[node];
        }

        //If the node does not have any children, then the B-Level is its duration.
        List<Integer> childrenList  = taskGraph.getChildrenList(node);
        if(childrenList.isEmpty()){
            lengths[node] = taskGraph.getDuration(node);
            return lengths[node];
        }

        //If this node has children, then we must get the B-levels its children and use the max.
        int maxLength = 0;
        for(int child : childrenList){
            maxLength = Math.max(maxLength, bLevels(child, lengths, taskGraph));
        }

        lengths[node] = maxLength + taskGraph.getDuration(node);
        return lengths[node];
    }

    /**
     * This method, for every node, finds the set of nodes that are equivalent to it.
     *
     * @param taskGraph an object that contains information about the DAG
     * @return an array of integer arraylists that for index i contains a list of nodes equivalent to node i.
     */
    public static ArrayList<Integer>[] getNodeEquivalence(TaskGraph taskGraph){
        HashSet<Integer> seen = new HashSet<>();
        int numTasks = taskGraph.getNumberOfTasks();
        ArrayList<Integer>[] equivalentNodesList = new ArrayList[numTasks];

        for(int i = 0; i<numTasks; i++) {
            //If we have seen a task before, then we already know the list of nodes it is equivalent to it.
            if (!seen.contains(i)) {
                ArrayList<Integer> equivalentNodes = new ArrayList<>();
                equivalentNodes.add(i);
                //Go through all the tasks and check for equality
                for (int j = 0; j < numTasks; j++) {
                    //If we have seen this node before, then we can be sure that it is not equal.
                    if (j != i && !seen.contains(j)) {
                        boolean equivalent = compare(i, j, taskGraph);
                        if (equivalent) {
                            equivalentNodes.add(j);
                        }
                    }
                }
                //For the list of equivalent nodes to i, associate their corresponding index in equivalentNodesList with
                //the equivalent nodes list.
                for (int j = 0; j < equivalentNodes.size(); j++) {
                    equivalentNodesList[equivalentNodes.get(j)] = equivalentNodes;
                    seen.add(equivalentNodes.get(j));
                }
            }
        }
        return equivalentNodesList;
    }

    /**
     * This method checks if two nodes are equivalent.
     *
     * @param a first node that we are comparing
     * @param b second node that we are comparing
     * @param taskGraph an object that contains information about the DAG
     * @return
     */
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
        Collections.sort(aParents);
        Collections.sort(bParents);
        for(int i = 0; i<aParents.size(); i++){
            int aParent = aParents.get(i);
            int bParent = bParents.get(i);
            if((aParent != bParent) ||taskGraph.getCommCost(aParent, a) != taskGraph.getCommCost(bParent, b)){
                return false;
            }
        }
        Collections.sort(aChildren);
        Collections.sort(bChildren);
        for(int i = 0; i<aChildren.size(); i++){
            int aChild = aChildren.get(i);
            int bChild= bChildren.get(i);
            if( (aChild != bChild) || taskGraph.getCommCost(a, aChild) != taskGraph.getCommCost(b, bChild)){
                return false;
            }
        }

        return true;
    }
}
