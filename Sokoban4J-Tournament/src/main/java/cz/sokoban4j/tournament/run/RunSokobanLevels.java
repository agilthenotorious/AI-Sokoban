package cz.sokoban4j.tournament.run;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import cz.cuni.amis.utils.process.ProcessExecution;
import cz.cuni.amis.utils.process.ProcessExecutionConfig;
import cz.cuni.amis.utils.simple_logging.SimpleLogging;
import cz.sokoban4j.Sokoban;
import cz.sokoban4j.SokobanConfig;
import cz.sokoban4j.SokobanConsole;
import cz.sokoban4j.simulation.SokobanResult.SokobanResultType;

/**
 * Runs {@link SokobanLevels} executing SEPARATE JVM for every {@link SokobanLevels#levels} sequentially.
 * Stops executing levels once an agent fails to solve the level.
 *  
 * @author Jimmy
 */
public class RunSokobanLevels {
	
	private SokobanLevels levels;
	
	private String agentClass;
	
	private File resultFile;
	
	private SokobanConfig config;
	
	private String[] extraJavaArgs;
	
	private int maxFail;
	
	public RunSokobanLevels(SokobanConfig config, String agentClass, SokobanLevels levels,
			                File resultFile, String[] extraJavaArgs, int maxFail) {
		super();
		this.config = config;
		this.agentClass = agentClass;
		this.levels = levels;
		this.resultFile = resultFile;
		this.extraJavaArgs = extraJavaArgs;
		this.maxFail = maxFail;
	}

	public void run() {
		SimpleLogging.initLogging();
		
		int failed = 0;
		
		for (int i = 0; i < levels.levels.size(); ++i) {			
	    	SokobanLevel level = levels.levels.get(i);
	    	
			ProcessExecutionConfig processConfig = new ProcessExecutionConfig();
			
			// ADD PROGRAM TO START
			processConfig.setPathToProgram("java");
			
			// CONFIGURE PROGRAM PARAMS
			List<String> args = new ArrayList<String>();
			
			// READ JAVA PARAMS
			RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
	    	List<String> jvmArgs = runtimeMXBean.getInputArguments();
	    	for (String arg : jvmArgs) {
	    		if (arg.contains("agentlib") && arg.contains("suspend")) {
	    			// ECLIPSE DEBUGGING, IGNORE
	    			continue;
	    		}
	    	    args.add(arg);
	    	}
	    	
	    	// ADD EXTRA JAVA PARAMS
	    	if (extraJavaArgs != null && extraJavaArgs.length > 0) {
	    		for (String extraArg : extraJavaArgs) {
	    			args.add(extraArg);
	    		}
	    	}
	    	
	    	// ADD CLASS TO RUN	    	
	    	args.add("cz.sokoban4j.SokobanConsole");
	    	
	    	// ADD SOKOBAN CONSOLE ARGUMENTS	    	
			config.level = level.file;
			config.levelFormat = null;
			config.levelNumber = level.levelNumber;
			
			String[] consoleArgs = SokobanConsole.getArgs(config, agentClass, resultFile);
			
	    	for (String arg : consoleArgs) {
	    		args.add(arg);
	    	}
	    	
	    	processConfig.setArgs(args);
	    	
		// SET DIRECTORY
	    	processConfig.setExecutionDir(Sokoban.projectRoot());

	    	processConfig.setId("S4J");		
	    	
	    	processConfig.setRedirectStdOut(true);
	    	processConfig.setRedirectStdErr(true);
	    	
	    	// RUN THE PROCESS
	    	ProcessExecution execution = new ProcessExecution(processConfig, Logger.getAnonymousLogger());
	    	
	    	System.out.println("");
	    	System.out.println("===============================================");
	    	System.out.println("===============================================");
	    	System.out.println("RUNNING " + (i+1) + " / " + levels.levels.size() + " FOR " + agentClass);
	    	System.out.println("===============================================");
	    	System.out.println("===============================================");	    	
	    	
	    	execution.start();
	    	execution.getRunning().waitFor(false);
			
	    	if (execution.getExitValue() > 0) {
	    		System.out.println("========================================================");
		    	System.out.println("AGENT FAILED TO SOLVE THE LEVEL " + (i+1));
		    	System.out.println(level.file.getName() + " / " + level.levelNumber);
		    	System.out.println("Exit code: " + execution.getExitValue() + " ~ " + SokobanResultType.getForExitValue(execution.getExitValue()));
		    	System.out.println("========================================================");
	    		if (++failed == maxFail)
	    		    break;
	    	} else if (execution.isFailed()) {
	    		System.out.println("==================");
		    	System.out.println("EXECUTION FAILURE!");
		    	System.out.println("==================");
		    	break;
	    	}
		}
		
	}
	
}
