import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * The TaskGraph class encapsulates tasks and their dependencies.
 */
public class TaskGraph {
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
     * @return the number of tasks in the task graph.
     */
    public int getNumberOfTasks() {
        return numberOfTasks;
    }

    /**
     * @param task task to get parents of
     * @return list of parents of task
     */
    public List<Integer> getParentsList(int task) {
        return parentsList[task];
    }

    /**
     * @param task task to get children of
     * @return list of children of task
     */
    public List<Integer> getChildrenList(int task) {
        return childrenList[task];
    }

    /**
     * @param task task to get duration of
     * @return duration of specified task
     */
    public int getDuration(int task) {
        return durations[task];
    }

    /**
     * @param parent parent task
     * @param child task which depends on source
     * @return communication cost of scheduling dest on another processor from source
     * if (parent is not a parent of child), returns 0
     */
    public int getCommCost(int parent, int child) {
        return commCosts[parent][child];
    }
}