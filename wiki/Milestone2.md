# Milestone 2

The goal of Milestone 2 was to implement an algorithm which finds an optimal scheduling of tasks.
We also created a live visualisation of the algorithm, which can optionally be enabled.
The algorithm was also parallelised to run using a specified number of threads.

For more info on each of these aspects, se below:
- [Optimal Scheduling](./Optimal_Scheduling.md)
- [Visualisation](./Visualisation.md)
- [Parallelisation](./Parallelisation.md)

## Optimization

### Pre-Processing
* #### Node Duplication
* #### Initial Greedy Schedule
* #### Max Length to Exit Node

### Recursive Search and Backtracking

#### Order

### Pruning
Since our search space is exponential, we need to find methods to prune this search space such that it becomes manageable for us to search and find the optimal schedule.

* #### Equivalent Schedule
* #### Fixed Task Order (FTO)
   Suppose we are in the process of scheduling our tasks. Let us call the list of tasks where there are either no dependencies, or their dependencies have been completed, our list of candidateTasks. These are the tasks that can currently be scheduled.
   
   There are special structures that can be present in a task graph such that we can fix the order of our tasks.
   
   These structures must fulfill several conditions:
   1. All the candidateTasks must have at most one parent and at most one child.
   2. All the candidateTasks must have either no child or the same child.
   3. All the candidateTasks must have either no parent, or their parents are scheduled on the same processor.
   4. The candidateTasks list can be sorted such that the list fulfills the following two conditions. This will be the fixed task order.
        1. The tasks in the list are in non-decreasing data ready time. `Data ready time = finish time of parent + communication cost of parent to the task`.
        2. The tasks in the list are in non-increasing out-edge costs. `Out-edge cost = communication cost of task to its child`, or 0 if the task does not have a child.
     
   The fixed task order means that among the tasks in candidateTasks, an optimal solution should contain these tasks scheduled in this order. By fixing the task order, we are able to prune our tree by a factor of the number of tasks in candidateTasks, as we no longer need to check every single ordering. 
   
   The fixed task order works because scheduling tasks in non-decreasing data ready time ensures the minimalisation of idle time of processors, and the scheduling of tasks in non-increasing out-edge costs ensures that the start time of any tasks that depend on our set of candidateTasks (which should be all tasks that are not currently scheduled and not in candidateTasks by definition) can be minimalised. [More details here.](http://www.sciencedirect.com/science/article/pii/S0305054813002542)
   
   Once we get a FTO, we know that we can schedule the first task in our FTO safely. However, once the first task is scheduled, this may make changes to our list of candidate tasks. More specifically, if the task has a child, and the child becomes a candidate task, our candidateTasks may no longer form a valid FTO. For example, if the newly added child has two different children, our candidateTasks list would no longer satisfy condition i) for a FTO. If the scheduled task doesn't have a child however, our candidateTasks without the first scheduled task will still form a FTO because non of the conditions i), ii), iii) will be violated by the current tasks in candidateTasks, and the list is already in the order specified by iv).
   
* #### Load Balancing
    The load balanced time (LBT) is the minimum remaining time if all the remaining unscheduled tasks are spread evenly amongst the processors, not including communication costs. LBT = sum(unscheduled task durations) / number of processors.
    
    Since the LBT is a minimum bound on the finish time of the current schedule, if `LBT + earliest time we can schedule the next task` is slower than the current best schedule, we know that the current schedule can't become an optimal schedule and we can prune the tree.

* #### Critical Path
    The critical path is the path with the largest max length to an exit node in the current schedule. Since no matter what is in our schedule, this critical path will have nodes that depend on each other and hence can only be scheduled sequentially.
    
    The critical path is another minimum bound on the finish time of the current schedule. We prune the tree if the `critical path + current elapsed time >= finish time of best schedule` so far.
    
* #### Latest Processor Finishing Time
    The latest processor finishing time is the finishing time of the processor such that it is the largest among all processors. 
    
    The latest processor finishing time will then be a minimum bound on the finishing time of the current schedule. We stop considering this schedule if it is larger than the finish time of the current best schedule.

### Edge Cases
We check for edge cases in our algorithm to ensure that we can sort these edge cases in a faster way than other graphs.
#### Sequential
If there is only one processor, then all tasks should simply be scheduled sequentially on the processor with no idle time. The finish time of the optimal schedule is the sum of all the durations of the tasks. All we need to do is to find a valid order to schedule the tasks. 

## Acknowledgements
- Oliver Sinnen,
Reducing the solution space of optimal task scheduling,
Computers & Operations Research,
Volume 43,
2014,
Pages 201-214,
ISSN 0305-0548,
https://doi.org/10.1016/j.cor.2013.09.004.
(http://www.sciencedirect.com/science/article/pii/S0305054813002542)


