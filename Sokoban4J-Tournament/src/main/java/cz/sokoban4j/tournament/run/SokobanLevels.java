package cz.sokoban4j.tournament.run;

import java.io.File;
import java.util.*;

import cz.sokoban4j.Sokoban;
import cz.sokoban4j.SokobanConfig.ELevelFormat;

public class SokobanLevels {

	public List<SokobanLevel> levels = new ArrayList<SokobanLevel>();
	
	public void validate() {
		for (SokobanLevel level : levels) level.validate();
	}
	
	public static SokobanLevels fromString(String string) {
		SokobanLevels results = new SokobanLevels();
		
		String[] parts = string.split(";");
		
		if (parts.length % 2 != 0) {
			throw new RuntimeException("Level list string has invalid format; there is odd number of parts (should be even): " + parts.length);
		}
		
		int index = 0;
		
		while (index < parts.length) {
			
			String fileString = parts[index];
			
			File file = Sokoban.findFile(fileString);
			
			String level = parts[index + 1];
			
			if (level.toLowerCase().equals("all")) {
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					Arrays.sort(files);
					for (File childFile : files) {
						if (ELevelFormat.getExpectedLevelFormat(childFile) != null) {
							int levelCount = SokobanLevel.getLevelCount(childFile);
							for (int i = 1; i <= levelCount; ++i) {
								results.levels.add(new SokobanLevel(childFile, i));
							}
						}
					}
				} else
				if (file.isFile()) {
					int levelCount = SokobanLevel.getLevelCount(file);
					for (int i = 1; i <= levelCount; ++i) {
						results.levels.add(new SokobanLevel(file, i));
					}
				} else {
					throw new RuntimeException("Invalid file, neither file nor directory: " + file.getAbsolutePath());
				}
			} else {
				int levelNumber = Integer.parseInt(level);
				results.levels.add(new SokobanLevel(file, levelNumber));
			}
			
			index += 2;
		}
		
		return results;
	}
	
}
