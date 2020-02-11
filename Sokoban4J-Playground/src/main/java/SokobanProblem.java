import java.util.*;
import cz.sokoban4j.simulation.actions.EDirection;
import cz.sokoban4j.simulation.actions.compact.CMove;
import cz.sokoban4j.simulation.actions.compact.CPush;
import cz.sokoban4j.simulation.board.compact.BoardCompact;
import cz.sokoban4j.simulation.board.compact.CTile;

public class SokobanProblem implements HeuristicProblem<BoardCompact, EDirection>{
	private boolean[][] final_deadSquares; // if true -> square is Dead Square
	private boolean[][] dead_lock;	// if true -> square is Dead Lock
	private BoardCompact mainBoard;

	public SokobanProblem(BoardCompact board){
		mainBoard = board;
		final_deadSquares = new boolean[mainBoard.width()][mainBoard.height()];
		final_deadSquares = DeadSquareDetector.detect(mainBoard);
		dead_lock = new boolean[mainBoard.width()][mainBoard.height()];
		for(int i = 0; i < mainBoard.width(); i++){
			for(int j = 0; j < mainBoard.height(); j++) {
				if(final_deadSquares[i][j]) dead_lock[i][j] = false;
				else if(is_deadlock(i,j,mainBoard)) dead_lock[i][j] = true;
				else dead_lock[i][j] = false;

			}
		}
	}

	/* Given a state, we only consider directions where Sokoban either walks toward a box or pushes a box.
	   by "pushing a box", we only consider cases where it doesnt push box to Dead Square or push doesnt result in Dead Lock.
	   This optimisation will prune the search space of our original A* algorithm quite a bit.
	   For this optimisation we use BreadthFirstSearch (BFS) from Sokoban's position,looking for all boxes.
	   This will give shortest path from Sokoban to every box. If any of Sokoban's directions (UP,DOWN,LEFT,RIGHT) is not on one
	   of those shortest paths, it is useless and we dont add it to HashSet<EDirection>... */
	public HashSet<EDirection> walkstowards (BoardCompact state){
		HashSet<EDirection> resultdirections = new HashSet<>();
		EDirection[] directions = EDirection.arrows();
		LinkedList<NodeBFS<EDirection>> queue = new LinkedList<>();
		HashSet<Pos> visited = new HashSet<>();
		NodeBFS<EDirection> initial = new NodeBFS<>(state.playerX,state.playerY,null,null);
		queue.add(initial);
		while (!queue.isEmpty()) {
			NodeBFS<EDirection> currentNode = queue.poll();
			Pos currentNode_xy = new Pos(currentNode.x,currentNode.y);
			if(visited.contains(currentNode_xy)) continue;
			visited.add(currentNode_xy);
			for (EDirection direction : directions) {
				NodeBFS<EDirection> forwardNode = new NodeBFS<>(
						currentNode.x + direction.dX,
						currentNode.y + direction.dY,
						currentNode,
						direction
				);
				Pos forwardNode_xy = new Pos(forwardNode.x,forwardNode.y);
				if(CTile.isWall(state.tile(forwardNode.x, forwardNode.y)) || visited.contains(forwardNode_xy)) continue;
				if(!CTile.isSomeBox(state.tile(forwardNode.x, forwardNode.y))){
					queue.add(forwardNode);
				}
				if (CTile.isSomeBox(state.tile(forwardNode.x, forwardNode.y))
						&& !CTile.isSomeBox(state.tile(forwardNode.x+direction.dX,forwardNode.y+direction.dY))
						&& !final_deadSquares[forwardNode.x+direction.dX][forwardNode.y+direction.dY]
						&& !results_deadlock(forwardNode.x+direction.dX,forwardNode.y+direction.dY,state,direction)
				)
				{
					if (forwardNode.parent != null) {
						while (forwardNode.parent != null) {
							if (forwardNode.parent.parent == null) {
								resultdirections.add(forwardNode.action);
							}
							forwardNode = forwardNode.parent;
						}
					}
				}
			}
		}
		return resultdirections;
	}


	/*  Given (x,y) position, board state and EDirection, we check whether board state contains DeadLock situation or not
	    if we push box to that position.
		WHAT IS DEAD LOCK ? - A board situation is called a deadlock, if it makes the level unsolvable..
		In other words, if board state contains deadlock situation, then it isn’t possible to push every box to goal,anymore.
		Detecting deadlocks can prune huge parts of the search space and therefore is an important part of every solver.*/
	public boolean results_deadlock(int x, int y, BoardCompact board, EDirection dir) {
		EDirection[] directions = EDirection.arrows();
		for (EDirection direction : directions) {
			if(direction==dir.opposite()) continue;
			Pos currentNode = new Pos(x, y);
			Pos currentNode_CW = new Pos(currentNode.x + direction.cw().dX,currentNode.y + direction.cw().dY);
			Pos currentNode_CCW = new Pos(currentNode.x + direction.ccw().dX,currentNode.y + direction.ccw().dY);
			Pos forwardNode = new Pos(x + direction.dX, y + direction.dY);
			Pos forwardNode_CW = new Pos(forwardNode.x + direction.cw().dX,forwardNode.y + direction.cw().dY);
			Pos forwardNode_CCW = new Pos(forwardNode.x + direction.ccw().dX,forwardNode.y + direction.ccw().dY);
			if(dead_lock[currentNode.x][currentNode.y] && dead_lock[forwardNode.x][forwardNode.y] && CTile.isSomeBox(board.tile(forwardNode.x, forwardNode.y))      ) {
				if((CTile.isWall(board.tile(currentNode_CW.x, currentNode_CW.y))&&CTile.isWall(board.tile(forwardNode_CW.x, forwardNode_CW.y))&& !CTile.forSomeBox(board.tile(forwardNode.x, forwardNode.y)))
						||  (!CTile.forSomeBox(board.tile(forwardNode.x, forwardNode.y)) && CTile.isWall(board.tile(currentNode_CCW.x, currentNode_CCW.y))&&CTile.isWall(board.tile(forwardNode_CCW.x, forwardNode_CCW.y)))) {
					return true;
				}
			}
		}
		return false;
	}



	/*  Given (x,y) position and board state, we check whether that square is DeadLock or not.
		Knowing all DeadLock squares beforehand helps us to predict DeadLock situations before.
		We will use this method for execution of dead_lock[][] and results_deadlock().. */
	public boolean is_deadlock(int x, int y, BoardCompact board) {
		EDirection[] directions = EDirection.arrows();
		for (EDirection direction : directions) {
			Pos forwardNode = new Pos(x + direction.dX, y + direction.dY);
			Pos forwardCW = new Pos(x + direction.cw().dX, y + direction.cw().dY);
			Pos forwardCCW = new Pos(x + direction.ccw().dX, y + direction.ccw().dY);
			Pos wallCW = new Pos(forwardCW.x + direction.dX, forwardCW.y + direction.dY);
			Pos wallCCW = new Pos(forwardCCW.x + direction.dX, forwardCCW.y + direction.dY);
			if(CTile.isWall(board.tile(forwardNode.x , forwardNode.y)) && (      (CTile.isWall(board.tile(wallCW.x, wallCW.y))&&!final_deadSquares[forwardCW.x][forwardCW.y])       ||       (CTile.isWall(board.tile(wallCCW.x, wallCCW.y))&&!final_deadSquares[forwardCCW.x][forwardCCW.y])     )) {
				return true;
			}
		}
		return false;
	}

	public BoardCompact initialState() {
		return mainBoard;
	}

	public List<EDirection> actions(BoardCompact state) {
		List<EDirection> available_actions = new ArrayList<>();
		HashSet<EDirection> result_actions;
		result_actions = walkstowards(state);
		for(EDirection dirs :result_actions) {
			available_actions.add(dirs);
		}
		return available_actions;
	}

	public BoardCompact result(BoardCompact state, EDirection action) {
		BoardCompact bclone = state.clone();
		CPush mypush = CPush.getAction(action);
		CMove mymove = CMove.getAction(action);
		if(mymove.isPossible(bclone)) {
			mymove.perform(bclone);
			return bclone;
		}
		else{
			mypush.perform(bclone);
			return bclone;
		}
	}

	public boolean isGoal(BoardCompact state) {
		if(state.isVictory()) return true;
		return false;
	}

	public double cost(BoardCompact state, EDirection action) {
		return 1;
	}

	//Greedy method for heuristic
	//Our heuristic estimate is the sum of each box's distance to its nearest target, plus Sokoban's distance to the nearest box
	public int estimate(BoardCompact state) {
		List<int[]> myboxlist = new ArrayList<>();
		List<int[]> mytargetlist = new ArrayList<>();
		for (int y = 0; y < state.height(); ++y) {
			for (int x = 0; x < state.width(); ++x) {
				if (CTile.isSomeBox(state.tile(x, y))) {
					int[] a = new int[2];
					a[0] = x;
					a[1] = y;
					myboxlist.add(a);
				}
				if (CTile.forSomeBox(state.tile(x, y))) {
					int[] b = new int[2];
					b[0] = x;
					b[1] = y;
					mytargetlist.add(b);
				}
			}
		}
		int sokobantobox = 1000;
		int manhattansokoban;
		int totaldistanceboxtarget = 0;
		for (int i = 0; i < myboxlist.size(); i++) {
			int a[] = myboxlist.get(i);
			manhattansokoban = Math.abs(a[0] - state.playerX) + Math.abs(a[1] - state.playerY);
			if (manhattansokoban < sokobantobox) {
				sokobantobox = manhattansokoban;
			}
			int minimum = 1000;
			if (mytargetlist.size() == 0) break;
			for (int j = 0; j < mytargetlist.size(); j++) {
				int b[] = mytargetlist.get(j);
				int manhattandistance = Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]);
				if (manhattandistance == 0) {
					minimum = 0;
					break;
				}
				if (manhattandistance < minimum) {
					minimum = manhattandistance;
				}
			}
			totaldistanceboxtarget = totaldistanceboxtarget + minimum;
		}
		return totaldistanceboxtarget + sokobantobox;
	}
}