package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.aggregators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.DelayLine;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.DelayParameterTriple;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.DelayPoint;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts.StageCountsContainer;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.DelayMatrix7x7;
import de.uni_potsdam.hpi.asg.drivestrength.cells.TimingContainer;

public class CellDelayAggregator {

    private int inputSlewIndex;
    private AggregatedCell aggregatedCell;
    private StageCountsContainer stageCounts;

    public CellDelayAggregator(AggregatedCell aggregatedCell, StageCountsContainer stageCounts, int inputSlewIndex) {
        this.aggregatedCell = aggregatedCell;
        this.inputSlewIndex = inputSlewIndex;
        this.stageCounts = stageCounts;
    }

    public void run() {

        aggregatedCell.setSizeDelayLines(this.extractDelayLinesFor(aggregatedCell.getRawSizes(), aggregatedCell.getInputPinNames(),
                                                                   aggregatedCell.getSizeCapacitances()));


        Map<String, Integer> stageCountsPerPin = this.stageCounts.getFootprintDefaults().get(aggregatedCell.getName());
        aggregatedCell.setDelayParameterTriples(this.extractDelayParameters(aggregatedCell.getSizeDelayLines(), stageCountsPerPin));
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

        return sizeDelayLines;
    }

    private DelayLine extractDelayLineFor(String pinName, Cell rawSize, double inputCapacitance) {
        List<DelayLine> delayLines = new ArrayList<>();
        for (TimingContainer t : rawSize.getOutputPin().getTimings()) {
            if (!t.getRelatedPinName().equals(pinName)) continue;
            if (t.getRiseDelays() == null || t.getFallDelays() == null) continue;
            DelayLine lineRise = this.extractDelayLine(t.getRiseDelays(), inputCapacitance);
            DelayLine lineFall = this.extractDelayLine(t.getFallDelays(), inputCapacitance);
            delayLines.add(DelayLine.averageFrom(lineRise, lineFall));
        }
        if (delayLines.size() == 0) {
            throw new Error("Could not find delay slope for pin " + pinName + " on raw cell " + rawSize.getName());
        }
        return DelayLine.averageFrom(delayLines);
    }

    private DelayLine extractDelayLine(DelayMatrix7x7 delayMatrix, double inputCapacitance) {
        List<DelayPoint> delayPoints = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            double loadCapacitance = delayMatrix.getLoadCapacitanceAt(i);
            double electricalEffort = loadCapacitance / inputCapacitance;
            double delay = delayMatrix.getDelayAt(this.inputSlewIndex, i);

            delayPoints.add(new DelayPoint(electricalEffort, delay));
        }

        LinearFunctionFitter f = new LinearFunctionFitter(delayPoints);
        return f.getDelayLine();
    }

    private Map<String, DelayParameterTriple> extractDelayParameters(
               Map<String, Map<String, DelayLine>> delayLines, Map<String, Integer> stageCounts) {

        Map<String, DelayParameterTriple> delayParameterTriplesPerPin = new HashMap<>();

        for (String pinName : delayLines.keySet()) {
            Map<String, DelayLine> delayLinesForPin = this.filterOutDeviatingSizeDelayLines(delayLines.get(pinName));
            int stageCountForPin = stageCounts.get(pinName);
            delayParameterTriplesPerPin.put(pinName, new DelayParametersExtractor(delayLinesForPin, stageCountForPin).run());
        }

        return delayParameterTriplesPerPin;
    }

    private Map<String, DelayLine> filterOutDeviatingSizeDelayLines(Map<String, DelayLine> delayLines) {
        Map<String, DelayLine> filtered = new HashMap<>();
        for (String sizeName : delayLines.keySet()) {
            if (!this.isDeviatingSize(sizeName)) {
                filtered.put(sizeName, delayLines.get(sizeName));
            }
        }
        return filtered;
    }

    private boolean isDeviatingSize(String rawCellName) {
        return this.stageCounts.getDeviatingSizes().containsKey(rawCellName);
    }

}
