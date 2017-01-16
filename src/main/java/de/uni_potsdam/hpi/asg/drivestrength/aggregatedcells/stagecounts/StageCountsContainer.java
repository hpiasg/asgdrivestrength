package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts;

import java.util.HashMap;

public class StageCountsContainer {
	private HashMap<String, HashMap<String, Integer>> footprintDefaults;
	private HashMap<String, HashMap<String, Integer>> deviatingSizes;
	
	public StageCountsContainer() {
	}
	
	public HashMap<String, HashMap<String, Integer>> getFootprintDefaults() {
		return this.footprintDefaults;
	}
	
	public HashMap<String, HashMap<String, Integer>> deviatingSizes() { 
		return this.deviatingSizes;
	}
}
