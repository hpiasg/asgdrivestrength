package de.uni_potsdam.hpi.asg.drivestrength.cells.additionalinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdditionalCellInfoContainer {

    private HashMap<String, HashMap<String, Integer>> defaultStageCounts;
    private HashMap<String, HashMap<String, Integer>> deviatingStageCountSizes;
    private HashMap<String, Double> drivestrengthFanoutFactors;

    public AdditionalCellInfoContainer() {
    }

    public HashMap<String, HashMap<String, Integer>> getDefaultStageCounts() {
        return this.defaultStageCounts;
    }

    public HashMap<String, HashMap<String, Integer>> getDeviatingStageCountSizes() {
        return this.deviatingStageCountSizes;
    }

    public List<String> listDeviatingSizes() {
        return new ArrayList<String>(this.deviatingStageCountSizes.keySet());
    }

    public double getDrivestrengthFanoutFactorFor(String sizeName) {
        return drivestrengthFanoutFactors.get(sizeName);
    }
}
