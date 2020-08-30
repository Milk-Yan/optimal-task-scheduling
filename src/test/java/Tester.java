import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the optimality of the solution of the task scheduling problem.
 */
public class Tester {
    private final String graphDir = System.getProperty("user.dir") + System.getProperty("file.separator") + "src" +
            System.getProperty("file.separator") + "test" + System.getProperty("file.separator") + "graphs" +
            System.getProperty("file.separator");

    // ================================Tests on ONE processor======================================

    /**
     * Test an empty graph on one processor. It should give an empty output.
     */
    @Test
    public void testOneProcessorEmptyGraph() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "EmptyGraph.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 1, outputFileName));
        assertEquals(0, validator.getBestTime());
        cleanUp(outputFileName);
    }

    /**
     * Test a graph with only one node on one processor. It should give us a graph
     * with the finish time as the weight of the one node.
     */
    @Test
    public void testOneProcessorOneNode() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "1Node.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 1, outputFileName));
        assertEquals(21, validator.getBestTime());
        cleanUp(outputFileName);
    }

    /**
     * Test a simple graph without edges on one processor
     */
    @Test
    public void testOneProcessorNoEdges() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "5Nodes0Edges.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 1, outputFileName));
        assertEquals(154, validator.getBestTime());
        cleanUp(outputFileName);
    }

    /**
     * Test a simple graph on one processor with sequential edges A -> B -> C -> D -> E
     */
    @Test
    public void testOneProcessorSequentialEdges() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "5NodesSequentialEdges.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 1, outputFileName));
        assertEquals(154, validator.getBestTime());
        cleanUp(outputFileName);
    }

    /**
     * Test a simple graph on one processor with 7 edges.
     */
    @Test
    public void testOneProcessorSevenEdges() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "5Nodes7Edges.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 1, outputFileName));
        assertEquals(154, validator.getBestTime());
        cleanUp(outputFileName);
    }

    // ===============================Tests on MULTIPLE processors==================================

    /**
     * Test an empty graph on multiple processors. It should give an empty output.
     */
    @Test
    public void testMultipleProcessorsEmptyGraph() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "EmptyGraph.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 4, outputFileName));
        assertEquals(0, validator.getBestTime());
        cleanUp(outputFileName);
    }

    /**
     * Test a graph with only one node on multiple processors. It should give us a graph
     * with the finish time as the weight of the one node.
     */
    @Test
    public void testMultipleProcessorsOneNode() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "1Node.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 2, outputFileName));
        assertEquals(21, validator.getBestTime());
        cleanUp(outputFileName);
    }

    /**
     * Test a simple graph on multiple processors with sequential edges.
     * They should end up being scheduled on one processor due to
     * dependencies.
     */
    @Test
    public void testMultipleProcessorSequentialEdges() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "5NodesSequentialEdges.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 3, outputFileName));
        assertEquals(154, validator.getBestTime());
        cleanUp(outputFileName);
    }

    /**
     * Test a simple graph on multiple processors with no edges. The tasks
     * should be evenly scheduled on the processors.
     */
    @Test
    public void testMultipleProcessorZeroEdges() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "5Nodes0Edges.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 4, outputFileName));
        assertEquals(46, validator.getBestTime());
        cleanUp(outputFileName);
    }

    /**
     * Test a graph on multiple processors with maximal edges, i.e. if any further
     * edge is added, the graph would contain a cycle and no longer be valid.
     * The graph should then be scheduled the same as a sequential graph.
     */
    @Test
    public void testMultipleProcessorsMaximalEdges() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "5NodesMaximalEdges.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 4, outputFileName));
        assertEquals(154, validator.getBestTime());
        cleanUp(outputFileName);
    }

    /**
     * Test a sparse graph with fifteen nodes in it. Sparse graph means a graph
     * with not many edges.
     */
    @Test
    public void testSparseGraphFifteenNodes() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "15Nodes10Edges.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 3, outputFileName));
        assertEquals(197, validator.getBestTime());
        cleanUp(outputFileName);
    }

    /**
     * Test a dense graph with fifteen nodes in it. Dense graph means a graph
     * with many edges.
     */
    @Test
    public void testDenseGraphFifteenNodes() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "15Nodes80Edges.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 4, outputFileName));
        assertEquals(516, validator.getBestTime());
        cleanUp(outputFileName);
    }

    // ===============================Tests provided by client==================================

    /**
     * These tests cases were provided by the client.
     */

    @Test
    public void testProvidedSeven2() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "Nodes_7_OutTree.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 2, outputFileName));
        assertEquals(28, validator.getBestTime());
        cleanUp(outputFileName);
    }

    @Test
    public void testProvidedSeven4() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "Nodes_7_OutTree.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 4, outputFileName));
        assertEquals(22, validator.getBestTime());
        cleanUp(outputFileName);
    }

    @Test
    public void testProvidedEight2() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "Nodes_8_Random.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 2, outputFileName));
        assertEquals(581, validator.getBestTime());
        cleanUp(outputFileName);
    }

    @Test
    public void testProvidedEight4() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "Nodes_8_Random.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 4, outputFileName));
        assertEquals(581, validator.getBestTime());
        cleanUp(outputFileName);
    }

    @Test
    public void testProvidedNine2() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "Nodes_9_SeriesParallel.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 2, outputFileName));
        assertEquals(55, validator.getBestTime());
        cleanUp(outputFileName);
    }

    @Test
    public void testProvidedNine4() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "Nodes_9_SeriesParallel.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 4, outputFileName));
        assertEquals(55, validator.getBestTime());
        cleanUp(outputFileName);
    }

    @Test
    public void testProvidedTen2() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "Nodes_10_Random.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 2, outputFileName));
        assertEquals(50, validator.getBestTime());
        cleanUp(outputFileName);
    }

    @Test
    public void testProvidedTen4() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "Nodes_10_Random.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 4, outputFileName));
        assertEquals(50, validator.getBestTime());
        cleanUp(outputFileName);
    }

    @Test
    public void testProvidedEleven2() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "Nodes_11_OutTree.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 2, outputFileName));
        assertEquals(350, validator.getBestTime());
        cleanUp(outputFileName);
    }

    @Test
    public void testProvidedEleven4() {
        SolutionValidator validator = new SolutionValidator();

        String inputFileName = graphDir + "Nodes_11_OutTree.dot";
        String outputFileName = inputFileName.replace(".dot", "-output.dot");
        assertTrue(testValidity(validator, inputFileName, 4, outputFileName));
        assertEquals(227, validator.getBestTime());
        cleanUp(outputFileName);
    }


    // ----------------------------------------UTILITY-----------------------------------------------

    private boolean testValidity(SolutionValidator validator, String inputFileName, int numProcessors, String outputFileName) {
        try {
            Process process = Runtime.getRuntime().exec("java -jar scheduler.jar " +
                    inputFileName + " " + numProcessors + " -o " + outputFileName);
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Error waiting for scheduler to run programs");
            e.printStackTrace();
        }

        return validator.validate(inputFileName, outputFileName, numProcessors);
    }

    private void cleanUp(String outputFileName) {
        new File(outputFileName).delete();
    }
}
