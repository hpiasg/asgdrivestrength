package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts.StageCountsContainer;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.DelayMatrix;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Pin;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Pin.Direction;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Timing;

public class CellAggregator {
    private final List<Cell> rawCells;
    private Map<String, AggregatedCell> aggregatedCells;
    private StageCountsContainer stageCounts;
    
    // We use values for input slew = 0.0161238 ns, as it is closest to the 0.0181584ns used in cell pdfs
    private final int inputSlewIndex = 0; 
    
    public CellAggregator(List<Cell> rawCells, StageCountsContainer stageCounts) {
        this.rawCells = rawCells;
        this.stageCounts = stageCounts;
    }
    
    public AggregatedCellLibrary run() {
        this.aggregatedCells = new HashMap<>();

        for (Cell rawCell : rawCells) {
            if (!isFitForAggregation(rawCell)) continue;
            String cellFootprint = rawCell.getFootprint();
            if (!aggregatedCells.containsKey(cellFootprint)) {
                //System.out.println("new aggregated cell, footprint: " + cellFootprint);
                this.aggregatedCells.put(cellFootprint, new AggregatedCell(cellFootprint));
            }
            //System.out.println("adding rawCell " + cell.getName() + " to " + cellFootprint);
            AggregatedCell aggregatedCell = this.aggregatedCells.get(cellFootprint);
            aggregatedCell.addCellSize(rawCell);
        }
        
        for (AggregatedCell aggregatedCell : aggregatedCells.values()) {
            
            List<Cell> rawSizes = aggregatedCell.getSizesRaw();
            
            aggregatedCell.setInputPinNames(this.extractInputPinNames(rawSizes.get(0)));
            aggregatedCell.setSizeCapacitances(this.extractSizeCapacitances(rawSizes));
            
            Map<String, Map<String, DelayLine>> delayLines = 
                    this.extractDelayLinesFor(rawSizes, aggregatedCell.getInputPinNames(), aggregatedCell.getSizeCapacitances());
            
            Map<String, Integer> stageCountsPerPin = this.stageCounts.getFootprintDefaults().get(aggregatedCell.getName());
            aggregatedCell.setDelayParameterTriples(this.extractDelayParameters(delayLines, stageCountsPerPin));;
        }
   
        return new AggregatedCellLibrary(aggregatedCells);
    }

    private boolean isFitForAggregation(Cell rawCell) {
        int outputPinCount = 0;
        for (Pin pin : rawCell.getPins()) {
            if (pin.getDirection() == Direction.output) {
                outputPinCount++;
            }
        }
        return outputPinCount == 1 && rawCell.getInputPins().size() >= 1;
    }

    private List<String> extractInputPinNames(Cell rawCell) {
        List<String> pinNames = new ArrayList<>();
        for (Pin p : rawCell.getInputPins()) {
            pinNames.add(p.getName());
        }
        return pinNames;
    }

    //returns map:  pin->size->value
    private Map<String, Map<String, Double>> extractSizeCapacitances(List<Cell> sizesRaw) {
        Map<String, Map<String, Double>> sizeCapacitances = new HashMap<>();
        for (Cell rawCell : sizesRaw) {
            Map<String, Double> pinCapacitancesForSize = this.extractPinCapacitancesOfSize(rawCell);
            for (String pinName : pinCapacitancesForSize.keySet()) {
                if (!sizeCapacitances.containsKey(pinName)) {
                    sizeCapacitances.put(pinName, new HashMap<String, Double>());
                }
                sizeCapacitances.get(pinName).put(rawCell.getName(), pinCapacitancesForSize.get(pinName));
            }
        }
        return sizeCapacitances;
    }

    private Map<String, Double> extractPinCapacitancesOfSize(Cell rawCell) {
        Map<String, Double> pinCapacitances = new HashMap<>();
        for (Pin pin : rawCell.getInputPins()) {
            pinCapacitances.put(pin.getName(), pin.getCapacitance());
        }
        return pinCapacitances;
    }
    
    

    //returns map:  pin->size->value
    private Map<String, Map<String, DelayLine>> extractDelayLinesFor(List<Cell> rawSizes, List<String> pinNames,
                   Map<String, Map<String, Double>> sizeCapacitances) {
        Map<String, Map<String, DelayLine>> sizeDelayLines = new HashMap<>();
        
        for (String pinName : pinNames) {
            sizeDelayLines.put(pinName, new HashMap<String, DelayLine>());
            for (Cell rawSize : rawSizes) {
                String sizeName = rawSize.getName();
                DelayLine delayLine = this.extractDelayLineFor(pinName, rawSize, sizeCapacitances.get(pinName).get(sizeName)); 
                sizeDelayLines.get(pinName).put(sizeName, delayLine);
            }
        }

        System.out.println("Delay Lines for " + rawSizes.get(0).getFootprint());
        System.out.println(sizeDelayLines);
        
        return sizeDelayLines;
    }
    
    private DelayLine extractDelayLineFor(String pinName, Cell rawSize, double inputCapacitance) {
        for (Timing t : rawSize.getOutputPin().getTimings()) {
            if (!t.getRelatedPinName().equals(pinName)) continue;
            if (t.getRiseDelays() == null || t.getFallDelays() == null) continue;
            DelayLine lineRise = this.extractDelayLine(t.getRiseDelays(), inputCapacitance);
            DelayLine lineFall = this.extractDelayLine(t.getFallDelays(), inputCapacitance);
            return DelayLine.averageFrom(lineRise, lineFall);
        }
        throw new Error("Could not find delay slope for pin " + pinName + " on raw cell " + rawSize.getName());
    }
    
    private DelayLine extractDelayLine(DelayMatrix delayMatrix, double inputCapacitance) {
        int leftIndex = 0;
        int rightIndex = 6;
        
        double loadCapacitanceLeft = delayMatrix.getLoadCapacitanceAt(leftIndex);
        double electricalEffortLeft = loadCapacitanceLeft / inputCapacitance;
        double delayLeft = delayMatrix.getDelayAt(this.inputSlewIndex, leftIndex);
        
        double loadCapacitanceRight = delayMatrix.getLoadCapacitanceAt(rightIndex);
        double electricalEffortRight = loadCapacitanceRight / inputCapacitance;
        double delayRight = delayMatrix.getDelayAt(this.inputSlewIndex, rightIndex);
        
        double deltaY = delayRight - delayLeft;
        double deltaX = electricalEffortRight - electricalEffortLeft;
        
        double slope = deltaY / deltaX; 
        
        
        double deltaXToZero = -electricalEffortLeft;
        double deltaYToZero = deltaXToZero * slope;
        double offset = delayLeft + deltaYToZero;
        
        return new DelayLine(slope, offset);
    }
    

    
    private Map<String, DelayParameterTriple> extractDelayParameters(
               Map<String, Map<String, DelayLine>> delayLines, Map<String, Integer> stageCounts) {
        
        Map<String, DelayParameterTriple> delayParameterTriplesPerPin = new HashMap<>();
        
        for (String pinName : delayLines.keySet()) {
            delayParameterTriplesPerPin.put(pinName, new DelayParametersExtractor(delayLines.get(pinName), stageCounts.get(pinName)).run());
        }
        
        return delayParameterTriplesPerPin;
    }
}
