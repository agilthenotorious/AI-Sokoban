import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import cz.sokoban4j.agents.ArtificialAgent;
import cz.sokoban4j.simulation.actions.EDirection;
import cz.sokoban4j.simulation.board.compact.BoardCompact;
import cz.sokoban4j.simulation.board.compact.CTile;

class DeadSquareDetector {
	//returns 2D boolean array of Board Tiles
	//if tile is dead square -> true
	public static boolean[][] detect(BoardCompact board) {
		boolean[][] final_deadSquares;
		final_deadSquares = new boolean[board.width()][board.height()];
		for(int i = 0; i < board.width(); i++){
			for(int j = 0; j < board.height(); j++) {
				if(nearwall_deadsquare(i,j,board) == true) {
					final_deadSquares[i][j]=true;
				}
				else {
					if (canwegettogoal(i,j, board)==true) {
						final_deadSquares[i][j]=false;
					}
					else {
						final_deadSquares[i][j]=true;
					}
				}
			}
		}
		return final_deadSquares;
	}

	public static boolean nearwall_deadsquare(int i, int j, BoardCompact board){
		//check if tile is dead square. returns true -> tile is dead square
		//considers only squares near walls
		if(CTile.isWall(board.tile(i, j))) return true;
		if(
						((CTile.isWall(board.tile(i-1, j)) && CTile.isWall(board.tile(i, j-1))) ||
						(CTile.isWall(board.tile(i-1, j)) && CTile.isWall(board.tile(i, j+1))) ||
						(CTile.isWall(board.tile(i+1, j)) && CTile.isWall(board.tile(i, j-1))) ||
						(CTile.isWall(board.tile(i+1, j)) && CTile.isWall(board.tile(i, j+1)))) &&
						!CTile.forSomeBox(board.tile(i, j)))	return true;
		return false;
	}

	public static boolean canwegettogoal(int x, int y,BoardCompact board ) {
		//checks if we can get to target from this tile.
		//if yes, then it returns true -> it is not dead square.
		EDirection[] directions = EDirection.arrows();
		LinkedList<Pos> queue = new LinkedList<>();
		HashSet<Pos> visited = new HashSet<>();
		Pos initial = new Pos(x,y);
		queue.add(initial);
		while (!queue.isEmpty()) {
			Pos currentPos = queue.poll();
			if (CTile.forSomeBox(board.tile(currentPos.x, currentPos.y))) return true;
			for (EDirection direction : directions) {
				Pos backNode = new Pos(currentPos.x - direction.dX, currentPos.y - direction.dY);
				Pos forwardNode = new Pos(currentPos.x + direction.dX, currentPos.y + direction.dY);
				if (!CTile.isWall(board.tile(forwardNode.x, forwardNode.y))
						&&!CTile.isWall(board.tile(backNode.x, backNode.y))
						&&!visited.contains(forwardNode)
						&&!nearwall_deadsquare(forwardNode.x,forwardNode.y,board)
				) {
					queue.add(forwardNode);
				}
			}
			visited.add(currentPos);
		}
		return false;
	}
}

public class MyAgent extends ArtificialAgent {
	protected List<EDirection> result;
	protected BoardCompact board;
	@Override
	protected List<EDirection> think (BoardCompact board) {
		this.board = board;
		this.result = new ArrayList<>();

		HeuristicProblem<BoardCompact, EDirection> p = new SokobanProblem (board);
		Solution<BoardCompact, EDirection> myresult = AStar.search(p);

		for (int i=0;i<myresult.actions.size();i++) {
			result.add(myresult.actions.get(i));
		}
		if (result.size() == 0) {
			throw new RuntimeException("FAILED TO SOLVE THE BOARD...");
		}
		return result;
	}
}