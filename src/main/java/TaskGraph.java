import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * The TaskGraph class encapsulates tasks and their dependencies.
 */
public class TaskGraph {
    private Graph dotGraph;
    private int numberOfTasks;

    private List<Integer>[] parentsList;
    private List<Integer>[] childrenList;
    private int[] durations;
    private int[][] commCosts;

    /**
     * Transforms the original dot file data into data structures that we use.
     * @param dotGraph The original dot file data parsed by GraphStream.
     */
    public TaskGraph(Graph dotGraph){
        this.dotGraph = dotGraph;
        numberOfTasks = dotGraph.getNodeCount();
        initializeDataStructures(numberOfTasks);

        for(int i = 0; i < numberOfTasks; i++){
            Node node = dotGraph.getNode(i);
            durations[i] = ((Double)node.getAttribute("Weight")).intValue();

            node.enteringEdges().forEach(e -> {
                int s = e.getSourceNode().getIndex();
                int t = e.getTargetNode().getIndex();
                int commCost = ((Double)e.getAttribute("Weight")).intValue();
                parentsList[t].add(s);
                childrenList[s].add(t);
                commCosts[s][t] = commCost;
            });
        }
    }

    /**
     * Initializes a TaskGraph object from given fields.
     * Only used for testing purposes.
     */
    public TaskGraph(List<Integer>[] parentsList, List<Integer>[] childrenList, int[] durations, int[][] commCosts){
        this.numberOfTasks = parentsList.length;
        this.parentsList = parentsList;
        this.childrenList = childrenList;
        this.durations = durations;
        this.commCosts = commCosts;
    }

    /**
     * Initializes all data structures which store the input graphs information.
     * @param n Number of nodes in the input graph.
     */
    private void initializeDataStructures(int n) {
        parentsList = new List[n];
        childrenList = new List[n];
        durations = new int[n];
        commCosts = new int[n][n];

        for(int i = 0; i < n; i++) {
            parentsList[i] = new ArrayList<Integer>();
            childrenList[i] = new ArrayList<Integer>();
        }
    }

    /**
     * @return reference to the original dot file data parsed by GraphStream.
     */
    public Graph getDotGraph() {
        return dotGraph;
    }

    /**
     * @return the number of tasks in the task graph.
     */
    public int getNumberOfTasks() {
        return numberOfTasks;
    }

    /**
     * n = number of tasks.
     * @return an Array of size n:
     * parentsList[i] => List of all parents of task i
     */
    public List<Integer>[] getParentsList() {
        return parentsList;
    }

    /**
     * n = number of tasks.
     * @return an Array of size n:
     * childrenList[i] => List of all children of task i
     */
    public List<Integer>[] getChildrenList() {
        return childrenList;
    }

    /**
     * n = number of tasks.
     * @return an Array of size n:
     * durations[i] => (int) the length of task i
     */
    public int[] getDurations() {
        return durations;
    }

    /**
     * n = number of tasks.
     * @return an Array[][] of size n x n:
     * durations[i][j] => (int) the communication cost between task i -> j
     *
     * if (i is not a parent of j) durations[i][j] => 0
     */
    public int[][] getCommCosts() {
        return commCosts;
    }
}