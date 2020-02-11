import java.util.*;
import java.util.HashSet;
import java.util.PriorityQueue;


//implementation of A* algorithm

public class AStar<S,A> {
  public static <S, A> Solution<S, A> search(HeuristicProblem<S, A> prob) {
    Node<S, A> startnode = new Node<>(
            prob.initialState(),
            null,
            null,
            0
    );
    startnode.estimate = prob.estimate(startnode.state);
    startnode.fcost = startnode.cost + startnode.estimate;

    PriorityQueue<Node<S,A>> frontierPriorityQueue = new PriorityQueue<> ();
    HashSet<S> explored = new HashSet<>();
    frontierPriorityQueue.add(startnode);
    List<A> mylist = new ArrayList<>();

    while (!frontierPriorityQueue.isEmpty()) {
      Node<S, A> firstNode = frontierPriorityQueue.poll();
      if (explored.contains(firstNode.state)) continue;
      explored.add(firstNode.state);
      if (prob.isGoal(firstNode.state)) {
        Node<S,A> copycurrent = new Node<>(firstNode);
        if (copycurrent.parent != null) {
          while(copycurrent.parent != null) {
            mylist.add(copycurrent.action);
            copycurrent = copycurrent.parent;
          }
        }
        Collections.reverse(mylist);
        Solution<S,A> mysolution = new Solution<>(mylist,firstNode.state,firstNode.cost);
        return mysolution;
      }

      for (A i : prob.actions(firstNode.state)) {
        S nextstate = prob.result(firstNode.state, i);
        if (explored.contains(nextstate)) continue;
        Node<S,A> child = new Node<>(prob.result(firstNode.state, i), firstNode, i,firstNode.cost + prob.cost(firstNode.state, i));
        child.estimate = prob.estimate(child.state);
        child.fcost = child.cost + child.estimate;
        frontierPriorityQueue.add(child);
      }
    }
    return null;
  }
}