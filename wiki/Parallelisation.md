# Parallelization
Each thread will work on exploring different states, so we need to give then their 
independent copies of the state. To do this, we created a `SearchState` class which 
contains fields that keep track of the free tasks in the state, in-degrees of tasks, 
task start times, processors that the tasks are scheduled on, processor finish times, 
and the sum of all the durations of tasks yet to be scheduled. To cater to the 
independent copy needs of each thread, the `SearchState` class has a method which 
returns a deep copy of itself. 

## ForkJoinPool
To parallelize the algorithm, we used the `ForkJoinPool` and `RecursiveAction` classes. 
`ForkJoinPool` is a type of ExecutorService for running `ForkJoinTask`s, and a 
`RecursiveAction` is a class which implements the `ForkJoinTask`. 

`ForkJoinPool`s, unlike general ExecutorServices are optimized for tasks that create 
their own sub-tasks. This makes them useful when parallelizing our algorithm because 
from a given state, our algorithm will recursively produce new states and search them.

## Implementation
Additionally, `ForkJoinPool`s uses per-thread-queuing and work stealing. So, like with 
an executor service, a `ForkJoinPool` has a common queue that threads in the pool can 
get tasks from. Unlike an executor service, each thread has its own dequeue. When a 
thread is executing a `RecursiveSearch` task and forks (produces new tasks), those 
tasks are added to the threads own dequeue. This prevents threads from blocking unless 
work stealing where threads can take and execute tasks from another thread. The way we 
use `ForkJoinPool`s is by creating Recursive search tasks for all the states that we 
create from a given state and calling `ForkJoinPool`s `invokeAll()` method which will 
add it to the common queue of tasks in the pool. 

We create an inner class called `RecursiveSearch` which extends `RecursiveAction` and 
overrode the compute method. The `compute` method is what does the work of exploring a 
state, and recursively creates new `RecursiveSearch` tasks for each new state to be 
explored. The outer class essentially sets up the initial state and then hands control 
over to the innerclass. 

The fields `bestStartTime`, `bestScheduledOn`, `bestFinishTime` and `seenSchedules` are 
global variables that are used by all threads. Because of this, they are only used 
within synchronized blocks to prevent concurrent issues. We use class level locks to ensure all instances of `RecursiveSearch` are synchronized.
