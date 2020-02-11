import java.util.List;

public interface Problem<S, A> {
    S initialState();                //returns initial state when game begins
    List<A> actions(S state);        //list of available actions on given S state
    S result(S state, A action);     //returns state after A action performed on S state
    boolean isGoal(S state);         //is this state Goal State?
    double cost(S state, A action);  //returns cost when A action performed on S state

}