package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.HashMap;
import java.util.Map;

public class AggregatedCell {
    private String name;
    private Map<String, Map<String, Double>> logicalEfforts; //cell->pin->value, hopefully nearly cell-and-pin-invariant
    private Map<String, Map<String, Double>> inputCapacitances; //cell->pin->value, hopefully nearly pin-invariant
    private Map<String, Map<String, Double>> parasiticDelays; //cell->pin->value, hopefully nearly cell-and-pin-invariant
    
    public String getName() {
        return name;
    }

    public AggregatedCell(String name) {
        this.name = name;
        this.logicalEfforts = new HashMap<>();
        this.inputCapacitances = new HashMap<>();
        this.parasiticDelays = new HashMap<>();
    }
    
    public void addCellCapacitances(String cellName, Map<String, Double> inputCapacitances) {
        this.inputCapacitances.put(cellName, inputCapacitances);
    }

    public void addCellLogicalEfforts(String cellName, Map<String, Double> logicalEfforts) {
        this.logicalEfforts.put(cellName, logicalEfforts);
    }
    
    public void addCellParasiticDelays(String cellName, Map<String, Double> parasiticDelays) {
        this.parasiticDelays.put(cellName, parasiticDelays);
    }
    
}
