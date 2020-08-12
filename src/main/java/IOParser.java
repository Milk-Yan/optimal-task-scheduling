import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDOT;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDOT;

public class IOParser {
    private String fileName;
    private List<Integer>[] inList;
    private List<Integer>[] outList;
    private int[] durations;
    private int[][] commCosts;

    private Graph graph;

    public IOParser(String fileName) {
        this.fileName = fileName;
    }

    private void initializeDataStructures(int n) {
        inList = new List[n];
        outList = new List[n];
        durations = new int[n];
        commCosts = new int[n][n];

        for(int i = 0; i < n; i++) {
            inList[i] = new ArrayList<Integer>();
            outList[i] = new ArrayList<Integer>();
        }
    }


    public void read() {
        graph = new DefaultGraph("tempGraph");
        FileSource fileSource = new FileSourceDOT();

        try {
            fileSource.addSink(graph);
            fileSource.readAll(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
          fileSource.removeSink(graph);
        }

        int n = graph.getNodeCount();
        initializeDataStructures(n);

        for(int i = 0; i < n; i++){
            Node node = graph.getNode(i);
            durations[i] = ((Double)node.getAttribute("Weight")).intValue();

            node.enteringEdges().forEach(e -> {
                int s = e.getSourceNode().getIndex();
                int t = e.getTargetNode().getIndex();
                int commCost = ((Double)e.getAttribute("Weight")).intValue();

                inList[t].add(s);
                outList[s].add(t);
                commCosts[s][t] = commCost;
            });
        }
    }

    public List<Integer>[] getInList() {
        return inList;
    }

    public List<Integer>[] getOutList() {
        return outList;
    }

    public int[] getDurations() {
        return durations;
    }

    public int[][] getCommCosts() {
        return commCosts;
    }

    public void write(Task[] result) {
        int n = result.length;
        for(int i = 0; i < n; i++){
            Node node = graph.getNode(i);
            node.setAttribute("Start Time", result[i].startTime);
            node.setAttribute("Processor", result[i].processor);
        }

        FileSink file = new FileSinkDOT(true);
        try {
            file.writeAll(graph, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
