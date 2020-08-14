# Milestone 1
The goals of Milestone 1 were to implement an algorithm which finds a valid scheduling of tasks.
Optimality of the schedule was not important for this milestone.

## Planning
### Expected outcomes
- Design data structure for representing information from the input file and storing the outputted schedule.
- Choose strategy for generating valid schedule.
- Manage package structure including external libraries.
### Solution
- Implemented skeleton code for the project including an `IOParser` and `Solution` class.
- Created pseudocode for greedy algorithm which schedules task as early as possible.
- Used Maven to manage dependencies.
### Issues
- Project needs to be 'clone-and-compile ready'. This needs to be confirmed with the client that they can build
the project using Maven.

## I/O Parsing
### Expected outcomes
- Accept arguments (eg. number of processors) from the command line.
- Read in information of an input dot file of a specified name and store it in a data structure.
- Write information from a data structure representing a valid schedule into a dot file.

### Solution
- [GraphStream](http://graphstream-project.org/) was used to read and write the dot files.
- All methods to do with I/O parsing were extracted to an `IOParser` class.
- The `IOParser` class does not contain any processing of the schedule.
### Issues
- The nodes are written to the output file in a different order to which they were provided in the input file.
This was confirmed to be acceptable by the client in the design meeting (10/08).
- The weights of nodes are provided as integers in the input file, but are written by GraphStream as doubles.
This needs to be confirmed for validity by the client.

## Valid Schedule Implementation
### Expected outcomes
- Accept data structures representing tasks, their weights, their dependencies, and the number of processors (p)
as input to the algorithm
- Implement an algorithm which generates a valid schedule and outputs it as a data structure.
### Solution
- Keep track of in-degrees of each node (task) using an array. Maintain the ones with an in-degree of 0 in a queue.
- Via an n-by-p array, keep track of, for each node, the earliest time it can be scheduled on each processor.
- Schedule a random task from queue and update the n-by-p array. Decrement in degree of children and
 add them to queue if the in degree becomes 0. Repeat until the queue is empty.
- All functionality is encapsulated in a `Solution` class.
### Issues
- The solution is not yet optimal.
- The array data structures could be abstracted to classes.

## Validity Testing
### Expected outcomes
### Solution
### Issues
