package de.uni_potsdam.hpi.asg.drivestrength.cells.additionalinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdditionalCellInfoContainer {

    private HashMap<String, HashMap<String, Integer>> defaultStageCounts;
    private HashMap<String, HashMap<String, Integer>> deviatingStageCountSizes;
    HashMap<String, String> defaultSizes;
    private HashMap<String, ArrayList<String>> orderedSizes;
    
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
    
    
    
    public ArrayList<String> getOrderedSizesFor(String footprint) {
        return orderedSizes.get(footprint);
    }

    public void removeOrderedSize(String size) {
        for (List<String> sizes: orderedSizes.values()) {
            sizes.remove(size);
        }
    }

    public String listOrderedSizes() {
        return orderedSizes.toString();
    }
    

    
    public String getDefaultSizeFor(String footprint) {
        return defaultSizes.get(footprint);
    }
}
