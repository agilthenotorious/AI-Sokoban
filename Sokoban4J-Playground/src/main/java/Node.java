public class Node<S, A> implements Comparable<Node<S,A>>{
    public S state;            // board state of node
    public Node<S, A> parent;  // parent node
    public A action;           // action we took to get here from parent node
    public double cost;        // cost of node
    public int estimate;       // heuristic estimation of node
    public double fcost;       // total cost of node

    public Node(S nodestate, Node<S, A> nodeparent, A nodeaction, double nodecost) {
        this.state = nodestate;
        this.parent = nodeparent;
        this.action = nodeaction;
        this.cost = nodecost;
    }

    public Node(Node<S,A> another) {
        this.state = another.state;
        this.parent = another.parent;
        this.action = another.action;
        this.cost = another.cost;
        this.estimate = another.estimate;
        this.fcost = another.fcost;
    }

    @Override
    public int compareTo(Node<S, A> compareToNode) {
        if ( this.fcost > compareToNode.fcost){
            return 1;
        } else {
            if( this.fcost< compareToNode.fcost) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}