package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.io.File;
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
    private final Map<String, Map<String, List<Double>>> delaysOverEffort;
    private StageCountsContainer stageCounts;
    
    // We use values for input slew = 0.0161238 ns, as it is closest to the 0.0181584ns used in cell pdfs
    private final int inputSlewIndex = 0; 
    
    public CellAggregator(List<Cell> rawCells, StageCountsContainer stageCounts) {
        this.rawCells = rawCells;
        this.delaysOverEffort = new HashMap<String, Map<String,List<Double>>>();
        this.stageCounts = stageCounts;
    }
    
    public AggregatedCellLibrary run() {
        this.aggregatedCells = new HashMap<>();

        for (Cell rawCell : rawCells) {
            if (!isFitForAggregation(rawCell)) continue;
            String cellFootprint = rawCell.getFootprint();
            if (!aggregatedCells.containsKey(cellFootprint)) {
                //System.out.println("new aggregated cell, footprint: " + cellFootprint);
                Map<String, Integer> stageCountsPerPin = stageCounts.getFootprintDefaults().get(cellFootprint);
                this.aggregatedCells.put(cellFootprint, new AggregatedCell(cellFootprint, stageCountsPerPin));
            }
            //System.out.println("adding rawCell " + cell.getName() + " to " + cellFootprint);
            AggregatedCell aggregatedCell = this.aggregatedCells.get(cellFootprint);
            aggregatedCell.addCellSize(rawCell);
        }
        
        for (AggregatedCell aggregatedCell : aggregatedCells.values()) {
            List<Cell> sizesRaw = aggregatedCell.getSizesRaw();
            
            aggregatedCell.setInputPinNames(this.extractInputPinNames(sizesRaw.get(0)));
            aggregatedCell.setSizeCapacitances(this.extractSizeCapacitances(sizesRaw));
            System.out.println("size capacitances for " + aggregatedCell.getName() + " :");
            System.out.println(aggregatedCell.getSizeCapacitances());
            
            /* TODO: sort everything by input pin first */

            aggregatedCell.setLogicalEfforts(this.extractLogicalEfforts(sizesRaw, aggregatedCell.getSizeCapacitances(), 
                                                                        aggregatedCell.getStageCounts()));
            aggregatedCell.setParasiticDelays(this.extractParasiticDelays(sizesRaw, aggregatedCell.getSizeCapacitances(), 
                                                                        aggregatedCell.getStageCounts()));
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
            String cellName = rawCell.getName();
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

    private Map<String, Double> extractLogicalEfforts(List<Cell> sizesRaw,
                   Map<String, Map<String, Double>> sizeCapacitances, Map<String, Integer> stageCounts) {
        Map<String, Double> logicalEffortsPerPin = new HashMap<>();
        return logicalEffortsPerPin;
    }
    

    private Map<String, Double> extractParasiticDelays(List<Cell> sizesRaw,
                   Map<String, Map<String, Double>> sizeCapacitances, Map<String, Integer> stageCounts) {
        Map<String, Double> parasiticDelaysPerPin = new HashMap<>();
        return parasiticDelaysPerPin;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private void preparePrintingDelaysAsJson(Cell cell, Map<String, Double> pinCapacitances) {
        Timing firstTiming = cell.getOutputPin().getTimings().get(0);
        if (firstTiming.getRiseDelays() == null || firstTiming.getFallDelays() == null) return;
        
        DelayMatrix delays = firstTiming.getRiseDelays();
        
        double inputCapacitance = pinCapacitances.get(firstTiming.getRelatedPinName());

        List<Double> delaysForOneSlew = new ArrayList<Double>();
        List<Double> electricalEfforts = new ArrayList<Double>();
        
        for (int loadIndex = 0; loadIndex < 7; loadIndex++) {
            double loadCapacitance = delays.getLoadCapacitanceAt(loadIndex);
            double electricalEffort = loadCapacitance / inputCapacitance;
            double delay = delays.getDelayAt(this.inputSlewIndex, loadIndex);

            delaysForOneSlew.add(delay);
            electricalEfforts.add(electricalEffort);
        }
        
        HashMap<String, List<Double>> packed = new HashMap<>();

        packed.put("electricalEfforts", electricalEfforts);
        packed.put("delays", delaysForOneSlew);
        
        String key = cell.getFootprint() + "." + cell.getName();
        
        delaysOverEffort.put(key, packed);
    }
    
    
    private Map<String, Double> extractLogicalEfforts(Cell rawCell, 
            Map<String, Double> pinCapacitances, Map<String, Integer> stageCounts) {
        Map<String, Double> logicalEfforts = new HashMap<>();
        
        for (Timing timing : rawCell.getOutputPin().getTimings()) {
            if (timing.getRiseDelays() == null || timing.getFallDelays() == null) continue;
            String inputPinName = timing.getRelatedPinName();
            double logicalEffortRise = this.extractLogicalEffort(timing.getRiseDelays(), pinCapacitances.get(inputPinName));
            double logicalEffortFall = this.extractLogicalEffort(timing.getFallDelays(), pinCapacitances.get(inputPinName));

//            System.out.println("g_rise=" + logicalEffortRise);
//            System.out.println("g_fall=" + logicalEffortFall);
//            System.out.println("(factor " + (logicalEffortRise / logicalEffortFall) + ")\n");

            logicalEfforts.put(inputPinName, (logicalEffortRise + logicalEffortFall)/2);
        }
        
        return logicalEfforts;
    }
    
    
    private double extractLogicalEffort(DelayMatrix delayMatrix, double inputCapacitance) {
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
        
        return deltaY / deltaX;
    }
    

    
    private Map<String, Double> extractParasiticDelays(Cell rawCell,
            Map<String, Double> pinCapacitances,
            Map<String, Double> logicalEfforts) {
        
        Map<String, Double> parasiticDelays = new HashMap<>();
        
        for (Timing timing : rawCell.getOutputPin().getTimings()) {
            if (timing.getRiseDelays() == null || timing.getFallDelays() == null) continue;
            String inputPinName = timing.getRelatedPinName();
            double parasiticDelayRise = this.extractParasiticDelay(timing.getRiseDelays(),
                                               pinCapacitances.get(inputPinName), logicalEfforts.get(inputPinName));
            double parasiticDelayFall = this.extractParasiticDelay(timing.getFallDelays(),
                                               pinCapacitances.get(inputPinName), logicalEfforts.get(inputPinName));

//            System.out.println("p_rise=" + parasiticDelayRise);
//            System.out.println("p_fall=" + parasiticDelayFall);
//            System.out.println("(factor " + (parasiticDelayRise / parasiticDelayFall) + ")\n");

            parasiticDelays.put(inputPinName, (parasiticDelayRise + parasiticDelayFall)/2);
        }
        
        return parasiticDelays;
    }

    private double extractParasiticDelay(DelayMatrix delayMatrix,
            Double inputCapacitance, Double logicalEffort) {
        
        int leftIndex = 0;
        
        double loadCapacitanceLeft = delayMatrix.getLoadCapacitanceAt(leftIndex);
        double electricalEffortLeft = loadCapacitanceLeft / inputCapacitance;
        double delayLeft = delayMatrix.getDelayAt(this.inputSlewIndex, leftIndex);
        
        double deltaX = -electricalEffortLeft;
        double deltaY = deltaX * logicalEffort; 
        
        return delayLeft + deltaY;
    }

    
    
}
