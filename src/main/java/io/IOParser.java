package io;

import java.io.IOException;

import data.Task;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDOT;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDOT;

/**
 * The io.IOParser class encapsulates the logic behind opening and reading a dot file into java data structures, and writing our results from java data structures to a dot file.
 *
 * Utilizes the GraphStream library found on GitHub at https://github.com/graphstream/gs-core, Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>.
 * The GraphStream license states: "Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed."
 * We have complied with their license by not modifying any of the files within the gs-core library.
 */
public class IOParser {

    /**
     * Reads a dot file into a data.TaskGraph object that encapsulates the initial tasks and their dependencies.
     * @param inputFileName The name of the input dot file.
     * @return Graph object that encapsulates the initial tasks and their dependencies
     */
    public static Graph read(String inputFileName) {
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

        return graph;
    }

    /**
     * Writes a set of results to the output dot file.
     * @param outputFileName The name of the output dot file.
     * @param dotGraph Graph object that encapsulates the initial tasks and their dependencies.
     * @param result List of scheduled tasks.
     */
    public static void write(String outputFileName, Graph dotGraph, Task[] result) {
        for(int i = 0; i < dotGraph.getNodeCount(); i++){
            Node node = dotGraph.getNode(i);
            Task task = result[i];
            node.setAttribute("Weight", task.getFinishTime() - task.getStartTime());
            node.setAttribute("Start", task.getStartTime());
            // the output uses processor numbers from 1, but the data is stored from 0
            node.setAttribute("Processor", task.getProcessor() + 1);
        }
        FileSink file = new FileSinkDOT(true);
        try {
            file.writeAll(dotGraph, outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
