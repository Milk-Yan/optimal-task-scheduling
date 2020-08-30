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

    // --------------------------------------BLACK BOX---------------------------------------------


    // ================================Tests on ONE processor======================================

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

    // --------------------------------------WHITE BOX----------------------------------------------


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
