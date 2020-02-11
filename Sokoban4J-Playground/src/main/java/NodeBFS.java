public class NodeBFS<EDirection>{
    public int x; // tile x
    public int y; // tile y
    public NodeBFS<EDirection> parent; // parent node
    public EDirection action;          // action we took to get here from parent node

    public NodeBFS(int x, int y, NodeBFS<EDirection> nodeparent, EDirection nodeaction) {
        this.x = x;
        this.y = y;
        this.parent = nodeparent;
        this.action = nodeaction;
    }

    public NodeBFS(NodeBFS<EDirection> another) {
        this.x = another.x;
        this.y = another.y;
        this.parent = another.parent;
        this.action = another.action;
    }

}