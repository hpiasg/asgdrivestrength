package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;

public class CellAggregator {
    private List<Cell> rawCells;
    private Map<String, AggregatedCell> aggregatedCells;
    
    public CellAggregator(List<Cell> rawCells) {
        this.rawCells = rawCells;
    }
    
    public Map<String, AggregatedCell> run() {
        this.aggregatedCells = new HashMap<>();
        
        for (Cell cell : rawCells) {
            String cellFootprint = cell.getFootprint();
            if (!aggregatedCells.containsKey(cellFootprint)) {
                System.out.println("new aggregated cell, footprint: " + cellFootprint);
                this.aggregatedCells.put(cellFootprint, new AggregatedCell(cellFootprint));
            }
            System.out.println("adding rawCell " + cell.getName() + " to " + cellFootprint);
        }
        
        
        return this.aggregatedCells;
    }
}
