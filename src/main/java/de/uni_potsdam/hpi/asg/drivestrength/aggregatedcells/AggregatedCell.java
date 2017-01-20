package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;

public class AggregatedCell {
    
    private String name;
    private Map<String, Map<String, Double>> sizeCapacitances; //pin->size->value
    private Map<String, DelayParameterTriple> delayParameterTriples; //pin->triple
    private List<String> inputPinNames;
    private List<String> sizeNames;
    private List<Cell> sizesRaw;
    
    public String getName() {
        return name;
    }

    public AggregatedCell(String name) {
        this.name = name;
        this.delayParameterTriples = new HashMap<>();
        this.sizeNames = new ArrayList<>();
        this.sizesRaw = new ArrayList<>();
    }
    
    public void setInputPinNames(List<String> inputPinNames) {
    	this.inputPinNames = inputPinNames;
    }
    
    public List<String> getInputPinNames() {
        return this.inputPinNames;
    }
    
    public void addCellSize(Cell cellSizeRaw) {
    	this.sizesRaw.add(cellSizeRaw);
        this.sizeNames.add(cellSizeRaw.getName());
    }
    
    public List<Cell> getRawSizes() {
    	return this.sizesRaw;
    }
    
    public boolean containsSizeName(String cellName) {
        return this.sizeNames.contains(cellName);
    }
    
    public Map<String, Map<String, Double>> getSizeCapacitances() {
		return sizeCapacitances;
	}

	public void setSizeCapacitances(Map<String, Map<String, Double>> sizeCapacitances) {
		this.sizeCapacitances = sizeCapacitances;
	}
	
	public void setDelayParameterTriples(Map<String, DelayParameterTriple> delayParameterTriples) {
	    this.delayParameterTriples = delayParameterTriples;
	}
	
	public Map<String, DelayParameterTriple> getDelayParameterTriples() {
	    return this.delayParameterTriples;
	}

    public double getParasiticDelayForPin(String pinName) {
        return this.delayParameterTriples.get(pinName).getParasiticDelay();
    }
    
    public double getLogicalEffortForPin(String pinName) {
        return this.delayParameterTriples.get(pinName).getLogicalEffort();
    }
    
    public double getStageCountForPin(String pinName) {
    	return this.delayParameterTriples.get(pinName).getStageCount();
    }
    
    public String getSizeNameFor(double inputPinCapacitance) {
    	String inputPinName = this.inputPinNames.get(0); 

        double bestAbsDiff = Double.POSITIVE_INFINITY;
        String bestSizeName = null; 
        for (String sizeName : this.sizeCapacitances.get(inputPinName).keySet()) {
            double cellAvgInputCapacitance = this.sizeCapacitances.get(inputPinName).get(sizeName);
            double absDiff = Math.abs(inputPinCapacitance - cellAvgInputCapacitance);
            if (absDiff < bestAbsDiff) {
                bestAbsDiff = absDiff;
                bestSizeName = sizeName;
            }
        }
        if (bestSizeName == null) {
            throw(new Error("Could not find " + this.getName() + " cell size for desired input pin capacitance " + inputPinCapacitance));
        }
        return bestSizeName;
    }
    
    public int getSizeCount() {
        return this.sizeNames.size();
    }
    
    public String toString() {
        return "AggregatedCell with " + this.getSizeCount() + " cell sizes";
    }
}
