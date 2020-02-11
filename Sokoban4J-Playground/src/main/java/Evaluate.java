import java.io.File;
import com.martiansoftware.jsap.JSAPException;
import cz.sokoban4j.tournament.SokobanTournamentConsole;

public class Evaluate {

	public static String[] LEVELS = new String[] { 
			"Easy/easy.sok;all",			   		   // 10 easy levels
			"sokobano.de/Aymeric_Medium.sok;all",    // 10 medium levels
			"sokobano.de/Aymeric_Hard.sok;all"      // 20 difficult levels
	};
	
	private static String getAllLevels() {
		StringBuffer result = new StringBuffer();
		for (String level : LEVELS) {
			result.append(";" + level);
		}
		result.delete(0, 1);
		return result.toString();
	}

	
	private static void evaluateLevels(Class<?> agentClass, boolean visualize, int maxFail) {
		String levels = getAllLevels();
		
		String ps = File.pathSeparator;
		
		try {
			SokobanTournamentConsole.main(
				new String[] {
					"-l", levels,
					"-r", "Sokoban4J-Playground/results/results-" + System.currentTimeMillis() + ".csv",
					"-t", "" + (6*1000),  // maximum of 6 seconds/level
					"-a", agentClass.getName(),
					"-v", "" + visualize, 
					"-f", "" + maxFail,
					"-i", agentClass.getSimpleName(),
					"-j", "-Xmx2g " +  // 2 GB maximum heap size 
					      "-cp Sokoban4J-Playground/target/classes"+ps+
						    "Sokoban4J-Tournament/target/classes"+ps+
					      "Sokoban4J/target/classes"+ps+
					      "Sokoban4J-Agents/target/classes"+ps+
					      "Sokoban4J-Tournament/libs/jsap-2.1.jar"+ps+
					      "Sokoban4J-Tournament/libs/process-execution-3.7.0.jar"+ps+
							  "Sokoban4J-Tournament/libs/xstream-1.3.1.jar"
				}
			);
		} catch (JSAPException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	
	public static void main(String[] args) {
		Class<?> agentClass = MyAgent.class;
		
		boolean visualize = false;
		
		int maxFail = 1;
		
		evaluateLevels(agentClass, visualize, maxFail);
	}
	
}
