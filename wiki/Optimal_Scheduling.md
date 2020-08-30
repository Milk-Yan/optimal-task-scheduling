# Optimal Scheduling

## Solution
[Need to talk about BnB, DFS here?]
## Optimisation
### Pre-Processing
* #### Node Duplication
    Nodes that are equivalent from the start of the algorithm will stay equivalent 
    throughout the run time of the algorithm. Therefore, it is safe to pre-calculate 
    for each node, the nodes that are equivalent to it. An array list of equivalent 
    nodes to node n are kept in index n in an array and used within the algorithm for 
    pruning/optimization purposes. 

* #### Initial Greedy Schedule

    Initially, when the algorithm runs, the current best finish time for any schedule 
    is infinity. Some pruning techniques under-estimate the finish time for a schedule 
    represented by this state and compare it to the current best if the estimate is 
    greater than the best it means that this state doesn't need to be further explored. 
    If the initial best time is infinity, then until a complete, valid schedule is found,
    the pruning techniques mentioned earlier will not have any effect. To get around 
    this problem, we run a greedy algorithm that recursively schedules the highest 
    priority free node to the earliest available processor until there are no more 
    tasks to schedule. At this point, there is a non-infinite valid schedule which will 
    be used as the initial best finish time for any schedule for the main algorithm. 

* #### Max Length to Exit Node
    The b-level of a node stays constant throughout the running of the algorithm. It is 
    safe to pre-calculate these and store them in an array. 

### Recursive Search and Backtracking
Initially, the given graph will have a set of nodes that can be run immediately. These 
nodes are passed into a recursive function. In turn, for every free task t, we try to 
schedule t on all processors p at the earliest possible time. Before the recursive call,
we update the state to reflect the fact that t has been scheduled corrected. This 
includes updating the set of free nodes, the time the processors are available, the 
in-degree of t's children and a few other pieces of information that is used for 
optimisation. Once the recursive call has returned, we revert the changes made to the 
state.

#### Order
Tasks are scheduled in order of their b-level value. Minimum b-level tasks will be 
scheduled before larger ones. This provides us with a heuristic to pick tasks, with
a higher probability of creating more-optimal schedules early, so we can use that 
information to prune less-optimal schedules, reducing the search space.

### Pruning
Since our search space is exponential, we need to find methods to prune this search 
space such that it becomes manageable for us to search and find the optimal schedule.

* #### Partial equivalent solutions
   
    The same order of tasks on processes may reoccur from the exploration of different 
    states.
    
    Given a set of free tasks `{a, b, c}`, suppose that we have already scheduled task `a` on 
    the processors up to index `i`. If `a` has no children, then scheduling `b` on any 
    processor less than `i` in the next recursive call will result in the algorithm 
    exploring the same partial state again. The reason for this is that node `b` will 
    eventually get to be scheduled on processors indexed less than `i-1` in the initial 
    state and when it goes through recursion, `a` will get to be scheduled on processors
    greater than the one `b` is on. 
    
    [add diagram here?]
    
* #### Fixed Task Order (FTO)
   Suppose we are in the process of scheduling our tasks. Let us call the list of tasks 
   where there are either no dependencies, or their dependencies have been completed, 
   our list of candidateTasks. These are the tasks that can currently be scheduled.
   
   There are special structures that can be present in a task graph such that we can 
   fix the order of our tasks.
   
   These structures must fulfil several conditions:
   1. All the candidateTasks must have at most one parent and at most one child.
   2. All the candidateTasks must have either no child or the same child.
   3. All the candidateTasks must have either no parent, or their parents are scheduled 
   on the same processor.
   4. The candidateTasks list can be sorted such that the list fulfils the following 
   two conditions. This will be the fixed task order.
        1. The tasks in the list are in non-decreasing data ready time. `Data ready 
        time = finish time of parent + communication cost of parent to the task`.
        2. The tasks in the list are in non-increasing out-edge costs. `Out-edge cost
        = communication cost of the task to its child`, or 0 if the task does not have 
        a child.
     
   The fixed task order means that among the tasks in candidateTasks, an optimal 
   solution should contain these tasks scheduled in this order. By fixing the task 
   order, we are able to prune our tree by a factor of the number of tasks in 
   candidateTasks, as we no longer need to check every single ordering. 
   
   The fixed task order works because scheduling tasks in non-decreasing data ready 
   time ensures the minimalization of idle time of processors, and the scheduling of 
   tasks in non-increasing out-edge costs ensure that the start time of any tasks 
   that depend on our set of candidateTasks (which should be all tasks that are not 
   currently scheduled and not in candidateTasks by definition) can be minimalised. 
   [More details here.](http://www.sciencedirect.com/science/article/pii/S0305054813002542)
   
   Once we get an FTO, we know that we can schedule the first task in our FTO safely. 
   However, once the first task is scheduled, this may make changes to our list of 
   candidate tasks. More specifically, if the task has a child, and the child becomes 
   a candidate task, our candidateTasks may no longer form a valid FTO. For example, 
   if the newly added child has two different children, our candidateTasks list would 
   no longer satisfy condition i) for an FTO. If the scheduled task doesn't have a child, 
   however, our candidateTasks without the first scheduled task will still form an FTO 
   because non of the conditions i), ii), iii) will be violated by the current tasks in 
   candidateTasks, and the list is already in the order specified by iv).
   
* #### Load Balancing
    The load balanced time (LBT) is the minimum remaining time if all the remaining 
    unscheduled tasks are spread evenly amongst the processors, not including 
    communication costs. LBT = sum(unscheduled task durations) / number of processors.
    
    Since the LBT is a minimum bound on the finish time of the current schedule, if 
    `LBT + earliest time we can schedule the next task` is greater than the current 
    best schedule, we know that the current schedule can't become an optimal schedule, 
    and we can safely return from this state.

* #### B-Levels
    A B-level of a node is the sum of its run time plus the maximum path to an exit 
    node from its self. We can use the B-level of a node to underestimate the 
    finishing time of the optimal schedule. 
    
    The underestimate for the finishing time of the optimal schedule if we want to 
    schedule task `i` on the processor `j` is: 
    `earliest Start Time of task i on processor j + B Level of task i`. 
    
    We can guarantee that this estimate is an underestimate because all the descendants 
    of node `i` must be scheduled strictly after the finish time of `i`. 
    
* #### Latest Processor Finishing Time
    The latest processor finishing time is the finishing time of a processor such that 
    it is the largest among all processors. 
    
    We stop considering this state if the largest processor finishing time is greater 
    than the current best schedule end time. The initial best schedule is our greedy
    one discussed above, and it gets updated if we find better schedules in our solution.

* #### Processor Normalization
    Two processors are isomorphic if they do not have any tasks scheduled on them. 
    Scheduling a task on multiple isomorphic processors produces the same resultant 
    state.
    
    In our algorithm, within a given state, we check if a task has been scheduled on 
    a processor with a finish time at time 0. If it has, and the current processor we 
    are considering to schedule it on is isomorphic we continue to the next processor. 
    
* #### State Duplication Avoidance
    If you hash a stack, its hashcode is dependent on the order of things in the stack, 
    when you hash a set, the order doesn't affect the hashcode. This means that we can 
    detect duplication that arises from swapping the tasks that have been scheduled on 
    two processors. 
    
    In our algorithm, we keep the hash codes of different states and check whether the 
    current state is a duplicate of one we have searched before.

* #### Processor Normalization
    Two processors are isomorphic if they do not have any tasks scheduled on them. 
    Scheduling a task on multiple isomorphic processors produces the same resultant 
    state.
    
    In our algorithm, within a given state, we check if a task has been scheduled on 
    a processor with a finish time at time 0. If it has, and the current processor we 
    are considering to schedule it on is isomorphic we continue to the next processor. 
    
* #### Node equivalence
    Two nodes are equivalent if they have the same duration, they have the same parents 
    and children and the edge costs between their parents and children are the same. 
    
    For a given state, there is no point in scheduling two identical tasks on the same 
    processor. In our algorithm, when considering if to schedule a task on a processor, 
    we check to see if we have already scheduled an equivalent task.
    

### Edge Cases
We check for edge cases in our algorithm to ensure that we can sort these edge cases in 
a faster way than other graphs.


* #### Sequential
    If there is only one processor, then all tasks should simply be scheduled 
    sequentially on the processor with no idle time. The finish time of the optimal 
    schedule is the sum of all the durations of the tasks. All we need to do is to 
    find a valid order to schedule the tasks. 
