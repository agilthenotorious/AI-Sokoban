import cz.sokoban4j.Sokoban;
import cz.sokoban4j.simulation.SokobanResult;

class RunMyAgent {
    public static void main(String[] args) {
		SokobanResult result;
		// VISUALIZED GAME
		//result = Sokoban.playAgentLevel("Easy/easy.sok",1, new MyAgent()); //There are 1-10 easy levels
		//result = Sokoban.playAgentLevel("sokobano.de/Aymeric_Medium.sok", 8, new MyAgent()); //There are 1-10 medium levels
		result = Sokoban.playAgentLevel("sokobano.de/Aymeric_Hard.sok", 1, new MyAgent()); //There are 1-20 hard levels
		System.out.println("MyAgent result: " + result.getResult());
		System.exit(0);
	}
}
