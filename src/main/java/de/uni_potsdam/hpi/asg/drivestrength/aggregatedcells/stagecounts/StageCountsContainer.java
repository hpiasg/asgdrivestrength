package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StageCountsContainer {
	private HashMap<String, HashMap<String, Integer>> footprintDefaults;
	private HashMap<String, HashMap<String, Integer>> deviatingSizes;

	public StageCountsContainer() {
	}

	public HashMap<String, HashMap<String, Integer>> getFootprintDefaults() {
		return this.footprintDefaults;
	}

	public HashMap<String, HashMap<String, Integer>> getDeviatingSizes() {
		return this.deviatingSizes;
	}

	public List<String> listDeviatingSizes() {
	    return new ArrayList<String>(this.deviatingSizes.keySet());
	}
}
