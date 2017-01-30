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
    private List<String> orderedPinNames;
    private List<String> inputPinNames;
    private String outputPinName;
    private List<String> sizeNames;
    private List<Cell> sizesRaw;
    private String defaultSizeName;
    
    public String getName() {
        return name;
    }

    public AggregatedCell(String name) {
        this.name = name;
        this.delayParameterTriples = new HashMap<>();
        this.sizeNames = new ArrayList<>();
        this.sizesRaw = new ArrayList<>();
    }
    
    public void setDefaultSizeName(String defaultSizeName) {
        this.defaultSizeName = defaultSizeName;
    }
    
    public void setInputPinNames(List<String> inputPinNames) {
    	this.inputPinNames = inputPinNames;
    }
    
    public List<String> getInputPinNames() {
        return this.inputPinNames;
    }
    
    public String getOutputPinName() {
        return outputPinName;
    }

    public void setOutputPinName(String outputPinName) {
        this.outputPinName = outputPinName;
    }
    
    public List<String> getOrderedPinNames() {
        return orderedPinNames;
    }

    public void setOrderedPinNames(List<String> orderedPinNames) {
        this.orderedPinNames = orderedPinNames;
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
    
    public double getAvgLogicalEffort() {
        double sum = 0;
        for (DelayParameterTriple t: this.delayParameterTriples.values()) {
            sum += t.getLogicalEffort();
        }
        return sum / this.delayParameterTriples.size();
    }
    
    public int getStageCountForPin(String pinName) {
    	return this.delayParameterTriples.get(pinName).getStageCount();
    }
    
    public String getSizeNameFor(Map<String, Double> desiredInputPinCapacitances) {
        double lowestDeviation = Double.POSITIVE_INFINITY;
        String bestSizeName = null;
        
        for (String sizeName : this.sizeNames) {
            double deviation = computeDeviationToDesiredCapacitances(sizeName, desiredInputPinCapacitances);
            if (deviation < lowestDeviation) {
                lowestDeviation = deviation;
                bestSizeName = sizeName;
            }
        }
        if (bestSizeName == null) {
            throw(new Error("Could not find " + this.getName() + " cell size for desired input pin capacitances"));
        }
        return bestSizeName;
    }
    
    private double computeDeviationToDesiredCapacitances(String sizeName, Map<String, Double> desiredCapacitances) {
        double deviation = 0.0;
        for (String pinName : desiredCapacitances.keySet()) {
            double actual = this.sizeCapacitances.get(pinName).get(sizeName);
            double desired = desiredCapacitances.get(pinName);
            deviation += Math.abs(actual - desired);
        }
        return deviation;
    }

    public double getLargestPossibleCapacitance(String pinName) {
        double largestC = 0.0;
        for (Cell rawCell : this.sizesRaw) {
            if (rawCell.getCapacitanceForPin(pinName) > largestC) {
                largestC = rawCell.getCapacitanceForPin(pinName);
            }
        }
        return largestC;
    }
    
    public double getSmallestPossibleCapacitance(String pinName) {
        double smallestC = Double.MAX_VALUE;
        for (Cell rawCell : this.sizesRaw) {
            if (rawCell.getCapacitanceForPin(pinName) < smallestC) {
                smallestC = rawCell.getCapacitanceForPin(pinName);
            }
        }
        return smallestC;
    }
    
    public int getSizeCount() {
        return this.sizeNames.size();
    }
    
    public String getPinNameAtPosition(int position) {
        return this.orderedPinNames.get(position);
    }
    
    public double getDefaultCapacitanceForPin(String pinName) {
        Cell rawCell = this.getDefaultSize();
        return rawCell.getCapacitanceForPin(pinName);
    }
    
    private Cell getDefaultSize() {
        for (Cell rawCell : this.sizesRaw) {
            if (rawCell.getName().equals(this.defaultSizeName)) {
                return rawCell;
            }
        }
        return this.sizesRaw.get(0);
    }
    
    public String toString() {
        return "AggregatedCell with " + this.getSizeCount() + " cell sizes";
    }
}
