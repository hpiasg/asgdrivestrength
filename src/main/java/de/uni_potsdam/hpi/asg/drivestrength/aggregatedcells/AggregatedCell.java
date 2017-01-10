package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregatedCell {
    
    private String name;
    private Map<String, Map<String, Double>> logicalEfforts; //cell->pin->value, hopefully nearly cell-and-pin-invariant
    private Map<String, Map<String, Double>> inputCapacitances; //cell->pin->value, hopefully nearly pin-invariant
    private Map<String, Map<String, Double>> parasiticDelays; //cell->pin->value, hopefully nearly cell-and-pin-invariant
    private List<String> sizeNames;
    
    public String getName() {
        return name;
    }

    public AggregatedCell(String name) {
        this.name = name;
        this.logicalEfforts = new HashMap<>();
        this.inputCapacitances = new HashMap<>();
        this.parasiticDelays = new HashMap<>();
        this.sizeNames = new ArrayList<>();
    }
    
    public void addCellSizeName(String cellName) {
        this.sizeNames.add(cellName);
    }
    
    public boolean containsSizeName(String cellName) {
        return this.sizeNames.contains(cellName);
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

    public Map<String, Map<String, Double>> getLogicalEfforts() {
        return logicalEfforts;
    }
    
    public double getAvgLogicalEffort() {
        int count = 0;
        double totalLogicalEffort = 0;
        for (Map<String, Double> logicalEffortsPerPin: this.logicalEfforts.values()) {
            for (double logicalEffort : logicalEffortsPerPin.values()) {
                count++;
                totalLogicalEffort += logicalEffort;
            }
        }
        return totalLogicalEffort / count;
    }
    
    public double getStdevLogicalEffort() {
        double average = this.getAvgLogicalEffort();
        
        int count = 0;
        double totalDev = 0;
        for (Map<String, Double> logicalEffortsPerPin: this.logicalEfforts.values()) {
            for (double logicalEffort : logicalEffortsPerPin.values()) {
                count++;
                totalDev += (logicalEffort - average) * (logicalEffort - average);
            }
        }
        double stdev = Math.sqrt(totalDev / count);
        
        return stdev;
    }
    
    public List<Double> getAvgLogicalEffortPerCell() {
        List<Double> avgLogicalEfforts = new ArrayList<>();
        for (Map<String, Double> logicalEffortsPerPin: this.logicalEfforts.values()) {
            int count = 0;
            double totalLogicalEffort = 0;
            for (double logicalEffort : logicalEffortsPerPin.values()) {
                count++;
                totalLogicalEffort += logicalEffort;
            }
            avgLogicalEfforts.add(totalLogicalEffort / count);
        }
        
        return avgLogicalEfforts;
    }

    public Map<String, Map<String, Double>> getInputCapacitances() {
        return inputCapacitances;
    }

    public Map<String, Map<String, Double>> getParasiticDelays() {
        return parasiticDelays;
    }
    
    public String getSizeNameFor(double inputPinCapacitance) {
        double bestAbsDiff = Double.POSITIVE_INFINITY;
        String bestSizeName = null; 
        for (String sizeName : this.inputCapacitances.keySet()) {
            double cellAvgInputCapacitance = averageInputCapacitance(sizeName);
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
    
    private double averageInputCapacitance(String sizeName) {
        Map<String, Double> cellInputCapacitances = this.inputCapacitances.get(sizeName);
        
        double sum = 0;
        int count = 0;
        for (double capacitance: cellInputCapacitances.values()) {
            sum += capacitance;
            count += 1;
        }
        
        return sum / count;
    }
    
}