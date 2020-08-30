import java.io.File;
import java.io.IOException;
import java.util.Random;

public class RandomGraphTester {

    private final static int NUM_TESTS = 10;

    public static void main(String[] args) throws IOException {
        SolutionValidater validater = new SolutionValidater();
        Random randomGenerator = new Random();

        File currentDir = new File(System.getProperty("user.dir"));
        for (File file: currentDir.listFiles()) {
            if (file.getName().matches("RandomGraph.*\\.dot")) {
                // only process dot files created from our generator
                int numProcessors = 1 + randomGenerator.nextInt(4);
                System.out.println("Testing " + file.getName() + " on " + numProcessors + " processors.");
                String outputFileName = file.getName().replace(".dot", "-output.dot");

                Process process = Runtime.getRuntime().exec("java -jar scheduler.jar " + file.getName() + " " +
                        numProcessors + " -o " + outputFileName);

                try {
                    process.waitFor();
                } catch (Exception e) {
                    System.err.println("Error waiting for scheduler to run programs");
                }

                if (validater.validate(file.getName(), outputFileName, numProcessors)) {
                    System.out.println("Validated.");
                } else {
                    System.out.println("The output is not valid!!");
                }
                file.delete();
                new File(outputFileName).delete();
            }
        }
    }
}
