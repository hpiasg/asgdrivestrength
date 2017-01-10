package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.DelayMatrix;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Pin;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Pin.Direction;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Timing;

public class CellAggregator {
    private final List<Cell> rawCells;
    private Map<String, AggregatedCell> aggregatedCells;
    private final Map<String, Map<String, List<Double>>> delaysOverEffort;
    
    // We use values for input slew = 0.0161238 ns, as it is closest to the 0.0181584ns used in cell pdfs
    private final int inputSlewIndex = 0; 
    
    public CellAggregator(List<Cell> rawCells) {
        this.rawCells = rawCells;
        this.delaysOverEffort = new HashMap<String, Map<String,List<Double>>>();
    }
    
    public AggregatedCellLibrary run() {
        this.aggregatedCells = new HashMap<>();

        for (Cell cell : rawCells) {
        	if (!isFitForAggregation(cell)) continue;
            String cellName = cell.getName();
            String cellFootprint = cell.getFootprint();
            if (!aggregatedCells.containsKey(cellFootprint)) {
                //System.out.println("new aggregated cell, footprint: " + cellFootprint);
                this.aggregatedCells.put(cellFootprint, new AggregatedCell(cellFootprint));
            }
            //System.out.println("adding rawCell " + cell.getName() + " to " + cellFootprint);
            AggregatedCell aggregatedCell = this.aggregatedCells.get(cellFootprint);
            aggregatedCell.addCellSizeName(cellName);
            Map<String, Double> pinCapacitances = this.extractPinCapacitances(cell);
            aggregatedCell.addCellCapacitances(cellName, pinCapacitances);

            this.printDelaysAsJson(cell, pinCapacitances);
            
            Map<String, Double> logicalEfforts = this.extractLogicalEfforts(cell, pinCapacitances);
            aggregatedCell.addCellLogicalEfforts(cellName, logicalEfforts);
            Map<String, Double> parasiticDelays = this.extractParasiticDelays(cell, pinCapacitances, logicalEfforts);
            aggregatedCell.addCellParasiticDelays(cellName, parasiticDelays);
        }
        

//        GsonBuilder builder = new GsonBuilder();
//        Gson gson = builder.create();
//        System.out.println(gson.toJson(delaysOverEffort));
    
        return new AggregatedCellLibrary(aggregatedCells);
    }
    
    private void printDelaysAsJson(Cell cell, Map<String, Double> pinCapacitances) {
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

	private Map<String, Double> extractPinCapacitances(Cell rawCell) {
        Map<String, Double> pinCapacitances = new HashMap<>();
        
        for (Pin pin : rawCell.getInputPins()) {
            pinCapacitances.put(pin.getName(), pin.getCapacitance());
        }
        
        return pinCapacitances;
    }
    
    private Map<String, Double> extractLogicalEfforts(Cell rawCell, Map<String, Double> pinCapacitances) {
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

    
    
    private boolean isFitForAggregation(Cell cell) {
        int outputPinCount = 0;
        for (Pin pin : cell.getPins()) {
            if (pin.getDirection() == Direction.output) {
                outputPinCount++;
            }
        }
        return outputPinCount == 1 && cell.getInputPins().size() >= 1;
    }
}
