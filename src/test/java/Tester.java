import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Tester {

    private final static int NUM_TESTS = 10;

    public static void main(String[] args) throws IOException {
        SolutionValidater validater = new SolutionValidater();
        Random randomGenerator = new Random();
        int validCount = 0;
        int totalCount = 0;

        File currentDir = new File(System.getProperty("user.dir"));
        for (File file: currentDir.listFiles()) {
            if (file.getName().matches("RandomGraph.*\\.dot")) {
                // only process dot files created from our generator
                int numProcessors = 1 + randomGenerator.nextInt(4);
                System.out.println("Testing " + file.getName() + " on " + numProcessors + " processors.");
                String outputFileName = file.getName().replace(".dot", "-output.dot");
                totalCount++;

                Process process = Runtime.getRuntime().exec("java -jar scheduler.jar " + file.getName() + " " +
                        numProcessors + " -o " + outputFileName);

                try {
                    process.waitFor();
                } catch (Exception e) {
                    System.err.println("Error waiting for scheduler to run programs");
                }

                if (validater.validate(file.getName(), outputFileName, numProcessors)) {
                    System.out.println("Validated.");
                    validCount++;
                } else {
                    System.out.println("The output is not valid!!");
                }
                file.delete();
                new File(outputFileName).delete();
            }
        }

        System.out.println(validCount + "/" + totalCount + " tests were valid.");
    }
}
