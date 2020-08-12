import java.util.List;

public class IOTester {

    /*
    The purpose of this class is to test that the input reader correctly reads and represents the given graph
    to the program and outputs the given attribute values for each node/task. This does not test correctness.
     */
    public static void main(String[] args){

        //Requires there to be a file called input.dot in the same dir
        IOParser parser = new IOParser("./input.dot");
        parser.read();
        List<Integer>[] inList = parser.getInList();
        List<Integer>[] outList = parser.getOutList();

        //Testing if the in degrees of each node which has a parent are correct.
        for(int i = 0; i < inList.length;i++){
            List<Integer> list = inList[i];
            System.out.print(i + ": ");
            for(int j = 0; j<list.size(); j++){
                System.out.print(list.get(j) + " ");
            }
            System.out.println("");
        }
        System.out.println("**************************************************");

        //Testing if the in out degree of each node which has a child(s) is correct.
        for(int i = 0; i < outList.length;i++){
            List<Integer> list = outList[i];
            System.out.print(i + ": ");
            for(int j = 0; j<list.size(); j++){
                System.out.print(list.get(j) + " ");
            }
            System.out.println("");
        }
        System.out.println("**************************************************");

        //Testing if weights of each node are read correctly.
        int[] weights = parser.getDurations();
        for(int i = 0; i < weights.length; i++){
            System.out.print(i + ": ");
            System.out.println(weights[i]);
        }

        //Printing out the communication costs.
        System.out.println("**************************************************");
        int[][] commCosts = parser.getCommCosts();
        for(int i = 0; i< commCosts.length; i++){
            for(int j = 0; j< commCosts[0].length; j++){
                if(commCosts[i][j] != 0){
                    System.out.println(i +  "->" + j + " = " + commCosts[i][j]);
                }
            }
        }

        //Creating random tasks
        Task[] tasks = new Task[]{
                new Task(0, 0, 2, 1),
                new Task(1, 2, 4, 2),
                new Task(2, 4, 10, 1),
                new Task(3, 9, 6, 2),
        };

        //The parser will write to the input file. Check the output file to ensure correctness.
        parser.write(tasks);
    }
}
