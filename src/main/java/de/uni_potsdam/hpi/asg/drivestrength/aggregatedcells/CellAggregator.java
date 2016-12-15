package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Pin;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Pin.Direction;

public class CellAggregator {
    private List<Cell> rawCells;
    private Map<String, AggregatedCell> aggregatedCells;
    
    public CellAggregator(List<Cell> rawCells) {
        this.rawCells = rawCells;
    }
    
    public Map<String, AggregatedCell> run() {
        this.aggregatedCells = new HashMap<>();
        
        for (Cell cell : rawCells) {
            if (!isFitForAggregation(cell)) continue;
            String cellName = cell.getName();
            String cellFootprint = cell.getFootprint();
            if (!aggregatedCells.containsKey(cellFootprint)) {
                System.out.println("new aggregated cell, footprint: " + cellFootprint);
                this.aggregatedCells.put(cellFootprint, new AggregatedCell(cellFootprint));
            }
            System.out.println("adding rawCell " + cell.getName() + " to " + cellFootprint);
            this.aggregatedCells.get(cellFootprint).addCellCapacitances(cellName, extractPinCapacitances(cell));
            
            //TODO: add parasitics + logical effort to this.aggregatedCells.get(cellFootprint).
        }
        
        
        return this.aggregatedCells;
    }
    
    private Map<String, Double> extractPinCapacitances(Cell rawCell) {
        Map<String, Double> pinCapacitances = new HashMap<>();
        
        for (Pin pin : rawCell.getPins()) {
            if (pin.getDirection() == Direction.input) {
                pinCapacitances.put(pin.getName(), pin.getCapacitance());
            }
        }
        
        return pinCapacitances;
    }
    
    private boolean isFitForAggregation(Cell cell) {
        int outputPinCount = 0;
        for (Pin pin : cell.getPins()) {
            if (pin.getDirection() == Direction.output) {
                outputPinCount++;
            }
        }
        return outputPinCount == 1;
    }
}
