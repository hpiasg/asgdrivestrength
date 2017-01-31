package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AggregatedCellLibrary {

    private Map<String, AggregatedCell> aggregatedCells;
    
    public AggregatedCellLibrary(Map<String, AggregatedCell> aggregatedCells) {
        this.aggregatedCells = aggregatedCells;
    }

    public AggregatedCell get(String aggregatedCellName) {
        AggregatedCell cell = this.aggregatedCells.get(aggregatedCellName);
        if (cell == null) {
            throw(new Error("Aggregated cell not in Library: " + aggregatedCellName));
        }
        return cell;
    }
    
    public List<AggregatedCell> getAll() {
        return new ArrayList<AggregatedCell>(this.aggregatedCells.values());
    }
    
    public AggregatedCell getByCellName(String cellName) {
        for (AggregatedCell aggregatedCell: this.aggregatedCells.values()) {
            if (aggregatedCell.containsSizeName(cellName)) {
                return aggregatedCell;
            }
        }
        throw(new Error("No aggregated cell for cell size name " + cellName));
    }
    public boolean isTieZero(String cellName) {
        return cellName.contains("TIE0");
    }
    
    public boolean isTieOne(String cellName) {
        return cellName.contains("TIE1");
    }
    
    public int size() {
        return this.aggregatedCells.size();
    }
    
    public String toString() {
        return "AggregatedCellLibrary (hashCode " + hashCode() +") with " + this.size() + " cells: " + this.aggregatedCells.toString();  
    }
    
    public void printDelayParameterTable() {
        System.out.println("Printing library delay paramters. Columns: ");
        System.out.println("CellFootprint, Pin, LogicalEffort, ParasiticDelay, StageCount");
        for (AggregatedCell cell : this.aggregatedCells.values()) {
            for (String pinName : cell.getDelayParameterTriples().keySet()) {
                DelayParameterTriple d = cell.getDelayParameterTriples().get(pinName);
                System.out.println(cell.getName() + ", " + pinName + ", " + d.getLogicalEffort() + ", " + d.getParasiticDelay() + ", " + d.getStageCount());
            }
        }
    }
}
