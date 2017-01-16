package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;

public class AggregatedCell {
    
    private String name;
    private Map<String, Double> logicalEfforts; // pin->value
    private Map<String, Double> parasiticDelays; //cell->pin->value, hopefully nearly cell-and-pin-invariant
    
    private Map<String, Map<String, Double>> sizeCapacitances; //pin->size->value
	private Map<String, Integer> stageCounts; //pin->value
    private List<String> inputPinNames;
    private List<String> sizeNames;
    private List<Cell> sizesRaw;
    
    public String getName() {
        return name;
    }

    public AggregatedCell(String name, Map<String, Integer> stageCounts) {
        this.name = name;
        this.logicalEfforts = new HashMap<>();
        this.parasiticDelays = new HashMap<>();
        this.sizeNames = new ArrayList<>();
        this.sizesRaw = new ArrayList<>();
        this.stageCounts = stageCounts;
    }
    
    public void setInputPinNames(List<String> inputPinNames) {
    	this.inputPinNames = inputPinNames;
    }
    
    public void addCellSize(Cell cellSizeRaw) {
    	this.sizesRaw.add(cellSizeRaw);
        this.sizeNames.add(cellSizeRaw.getName());
    }
    
    public List<Cell> getSizesRaw() {
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
    
    
    
    
    
    

    public void setLogicalEfforts(Map<String, Double> logicalEfforts) {
        this.logicalEfforts = logicalEfforts;
    }

    public Map<String, Double> getLogicalEfforts() {
        return logicalEfforts;
    }
    
    public void setParasiticDelays(Map<String, Double> parasiticDelays) {
        this.parasiticDelays = parasiticDelays;
    }
    
//    public double getAvgLogicalEffort() {
//        int count = 0;
//        double totalLogicalEffort = 0;
//        for (Map<String, Double> logicalEffortsPerPin: this.logicalEfforts.values()) {
//            for (double logicalEffort : logicalEffortsPerPin.values()) {
//                count++;
//                totalLogicalEffort += logicalEffort;
//            }
//        }
//        return totalLogicalEffort / count;
//    }
//    
//    public double getStdevLogicalEffort() {
//        double average = this.getAvgLogicalEffort();
//        
//        int count = 0;
//        double totalDev = 0;
//        for (Map<String, Double> logicalEffortsPerPin: this.logicalEfforts.values()) {
//            for (double logicalEffort : logicalEffortsPerPin.values()) {
//                count++;
//                totalDev += (logicalEffort - average) * (logicalEffort - average);
//            }
//        }
//        double stdev = Math.sqrt(totalDev / count);
//        
//        return stdev;
//    }
//    
//    public List<Double> getAvgLogicalEffortPerCell() {
//        List<Double> avgLogicalEfforts = new ArrayList<>();
//        for (Map<String, Double> logicalEffortsPerPin: this.logicalEfforts.values()) {
//            int count = 0;
//            double totalLogicalEffort = 0;
//            for (double logicalEffort : logicalEffortsPerPin.values()) {
//                count++;
//                totalLogicalEffort += logicalEffort;
//            }
//            avgLogicalEfforts.add(totalLogicalEffort / count);
//        }
//        
//        return avgLogicalEfforts;
//    }

    public Map<String, Double> getParasiticDelays() {
        return parasiticDelays;
    }
    
    public Map<String, Integer> getStageCounts() {
    	return stageCounts;
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
//    
//    private double averageInputCapacitance(String sizeName) {
//        Map<String, Double> cellInputCapacitances = this.inputCapacitances.get(sizeName);
//        
//        double sum = 0;
//        int count = 0;
//        for (double capacitance: cellInputCapacitances.values()) {
//            sum += capacitance;
//            count += 1;
//        }
//        
//        return sum / count;
//    }
    
}
