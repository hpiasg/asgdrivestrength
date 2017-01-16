package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

public class StageCountsParser {
	private File stageCountsFile;
	
	public StageCountsParser(File stageCountsFile) {
		this.stageCountsFile = stageCountsFile;
	}
	
	public StageCountsContainer run() {
		String json = readTextFile(stageCountsFile);
		StageCountsContainer c = new Gson().fromJson(json, StageCountsContainer.class);
		return c;
	}
	
	private String readTextFile(File textFile) {
		try {
			byte[] fileContents = Files.readAllBytes(Paths.get(textFile.getPath()));
			return new String(fileContents);
		} catch (IOException e) {
			throw new Error("Could not read file " +  textFile);
		}
	}
}
