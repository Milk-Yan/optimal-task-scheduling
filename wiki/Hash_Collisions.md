# Hash Collisions

The `hashcode()` method on a set returns an integer which can represents the hash code value for that set.
 
The hash code of a set is the sum of the hash codes of the elements in the set. This
ensures that `s1.equals(s2)` implies that `s1.hashCode()==s2.hashCode()` for any two sets `s1` and `s2`.
Thus, if two sets are equal, they will always return the same hash code. However, it is also possible
that two fundamentally different sets `x1` and `x2` return the same hash code.

With a sufficiently sized input graph, it is inevitable that different partial schedules return the same
hash code due to the pigeonhole principle. As such, it is possible that the solution incorrectly prunes
large sections of the graph which could contain the optimal solution.

However, we have still decided to use hash codes for duplication detection for the following reasons:

- We increased the hash collision resistance of our hash function by storing more data related to the
partial schedule being hashed. By storing more data, we reduce the risk
of generating the same hash code from two partial schedules which are not equal.

- Given the relatively small nodes and processors the solution is expected to be run on, the likelihood
of a scenario in which *all* optimal schedules in the graph are incorrectly detected as duplicates
is minimal.

- The solution run time is significantly improved through the use of state duplicate avoidance
via hash codes.

- Our implementation of state duplicate avoidance through the use of hash codes is easily detachable
from the rest of the solution if the client wishes to forego the sizable speed increases.
