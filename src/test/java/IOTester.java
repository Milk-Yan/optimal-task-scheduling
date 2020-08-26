import org.graphstream.graph.Graph;

import java.util.List;

public class IOTester {

    /*
    The purpose of this class is to test that the input reader correctly reads and represents the given graph
    to the program and outputs the given attribute values for each node/task. This does not test correctness.
     */
    public static void main(String[] args){

        //Requires there to be a file called input.dot in the same dir
        Graph dotGraph = IOParser.read("./input.dot");
        TaskGraph taskGraph = new TaskGraph(dotGraph);
        int numTasks = taskGraph.getNumberOfTasks();

        //Testing if the in degrees of each node which has a parent are correct.
        for(int i = 0; i < numTasks; i++){
            List<Integer> list = taskGraph.getParentsList(i);
            System.out.print(i + ": ");
            for(int j = 0; j<list.size(); j++){
                System.out.print(list.get(j) + " ");
            }
            System.out.println("");
        }
        System.out.println("**************************************************");

        //Testing if the in out degree of each node which has a child(s) is correct.
        for(int i = 0; i < numTasks; i++){
            List<Integer> list = taskGraph.getChildrenList(i);
            System.out.print(i + ": ");
            for(int j = 0; j<list.size(); j++){
                System.out.print(list.get(j) + " ");
            }
            System.out.println("");
        }
        System.out.println("**************************************************");

        //Testing if weights of each node are read correctly.
        for(int i = 0; i < numTasks; i++){
            System.out.print(i + ": ");
            System.out.println(taskGraph.getDuration(i));
        }

        //Printing out the communication costs.
        System.out.println("**************************************************");
        for(int i = 0; i < numTasks; i++){
            for(int j = 0; j < numTasks; j++){
                if(taskGraph.getCommCost(i,j) != 0){
                    System.out.println(i +  "->" + j + " = " + taskGraph.getCommCost(i,j));
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
        IOParser.write("./output.dot", dotGraph, tasks);
    }
}
