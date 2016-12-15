package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.HashMap;
import java.util.Map;

public class AggregatedCellLibrary {

    private Map<String, AggregatedCell> aggregatedCells;
    
    public AggregatedCellLibrary(Map<String, AggregatedCell> aggregatedCells) {
        this.aggregatedCells = new HashMap<String, AggregatedCell>();
    }

}
