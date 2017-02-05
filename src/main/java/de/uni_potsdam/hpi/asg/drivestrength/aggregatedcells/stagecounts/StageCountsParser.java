package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts;

import java.io.File;
import com.google.gson.Gson;
import de.uni_potsdam.hpi.asg.drivestrength.util.FileHelper;

public class StageCountsParser {
	private File stageCountsFile;
	
	public StageCountsParser(File stageCountsFile) {
		this.stageCountsFile = stageCountsFile;
	}
	
	public StageCountsContainer run() {
		String json = FileHelper.readTextFileToString(stageCountsFile);
		StageCountsContainer c = new Gson().fromJson(json, StageCountsContainer.class);
		return c;
	}
	
}
