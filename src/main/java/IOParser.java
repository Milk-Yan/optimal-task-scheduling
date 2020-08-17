import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDOT;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDOT;

/**
 * The IOParser class encapsulates the logic behind opening and reading a dot file into java data structures, and writing our results from java data structures to a dot file.
 *
 * Utilizes the GraphStream library found on GitHub at https://github.com/graphstream/gs-core, Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>.
 * The GraphStream license states: "Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed."
 * We have complied with their license by not modifying any of the files within the gs-core library.
 */
public class IOParser {

    /**
     * Reads a dot file into a TaskGraph object that encapsulates the initial tasks and their dependencies.
     * @param inputFileName The name of the input dot file.
     * @return TaskGraph object that encapsulates the initial tasks and their dependencies
     */
    public static TaskGraph read(String inputFileName) {
        Graph graph = new DefaultGraph("tempGraph");
        FileSource fileSource = new FileSourceDOT();

        try {
            fileSource.addSink(graph);
            fileSource.readAll(inputFileName);
        } catch (IOException e) {
            System.out.println("Error reading file: Please specify the path to a dot file");
        } finally {
          fileSource.removeSink(graph);
        }

        return new TaskGraph(graph);
    }



    /**
     * Writes a set of results to the output dot file.
     * @param outputFileName The name of the output dot file.
     * @param taskGraph TaskGraph object that encapsulates the initial tasks and their dependencies.
     * @param result List of scheduled tasks.
     */
    public static void write(String outputFileName, TaskGraph taskGraph, Task[] result) {
        int n = taskGraph.getNumberOfTasks();
        Graph dotGraph = taskGraph.getDotGraph();
        int[] taskDurations = taskGraph.getDurations();

        for(int i = 0; i < n; i++){
            Node node = dotGraph.getNode(i);
            node.setAttribute("Weight", taskDurations[i]);
            node.setAttribute("Start", result[i].startTime);
            node.setAttribute("Processor", result[i].processor);
        }
        FileSink file = new FileSinkDOT(true);
        try {
            file.writeAll(dotGraph, outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
