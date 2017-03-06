package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.defaultsizes.DefaultSizesContainer;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.orderedsizes.OrderedSizesContainer;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts.StageCountsContainer;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.DelayMatrix;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Pin;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Timing;

public class CellAggregator {
    protected static final Logger logger = LogManager.getLogger();

    private final List<Cell> rawCells;
    private Map<String, AggregatedCell> aggregatedCells;
    private StageCountsContainer stageCounts;
    private DefaultSizesContainer defaultSizes;
    private OrderedSizesContainer orderedSizes;
    private boolean skipDeviatingSizes;

    // We use values for input slew = 0.0161238 ns, as it is closest to the 0.0181584ns used in cell pdfs
    private final int inputSlewIndex = 0;

    public CellAggregator(List<Cell> rawCells, StageCountsContainer stageCounts,
            DefaultSizesContainer defaultSizes, OrderedSizesContainer orderedSizes, boolean skipDeviatingSizes) {
        this.rawCells = rawCells;
        this.stageCounts = stageCounts;
        this.defaultSizes = defaultSizes;
        this.orderedSizes = orderedSizes;
        this.skipDeviatingSizes = skipDeviatingSizes;
    }

    public AggregatedCellLibrary run() {
        this.aggregatedCells = new HashMap<>();

        for (Cell rawCell : rawCells) {
            if (!isFitForAggregation(rawCell)) continue;
            String cellFootprint = rawCell.getFootprint();
            if (!aggregatedCells.containsKey(cellFootprint)) {
                this.aggregatedCells.put(cellFootprint, new AggregatedCell(cellFootprint));
            }
            AggregatedCell aggregatedCell = this.aggregatedCells.get(cellFootprint);
            aggregatedCell.addCellSize(rawCell);
        }

        for (AggregatedCell aggregatedCell : aggregatedCells.values()) {

            List<Cell> rawSizes = aggregatedCell.getRawSizes();

            aggregatedCell.setOrderedPinNames(this.getOrderedPinNames(rawSizes.get(0)));
            aggregatedCell.setInputPinNames(this.extractInputPinNames(rawSizes.get(0)));
            aggregatedCell.setOutputPinName(this.extractOutputPinName(rawSizes.get(0)));
            aggregatedCell.setSizeCapacitances(this.extractSizeCapacitances(rawSizes));
            aggregatedCell.setDefaultSizeName(this.defaultSizes.get(aggregatedCell.getName()));
            aggregatedCell.orderRawSizes(this.orderedSizes.get(aggregatedCell.getName()));

            aggregatedCell.setSizeDelayLines(this.extractDelayLinesFor(rawSizes, aggregatedCell.getInputPinNames(),
                                                                       aggregatedCell.getSizeCapacitances()));

            Map<String, Integer> stageCountsPerPin = this.stageCounts.getFootprintDefaults().get(aggregatedCell.getName());
            aggregatedCell.setDelayParameterTriples(this.extractDelayParameters(aggregatedCell.getSizeDelayLines(), stageCountsPerPin));;
        }

        return new AggregatedCellLibrary(aggregatedCells);
    }

    private boolean isFitForAggregation(Cell rawCell) {
        String logPrefix = "Skipping cell " + rawCell.getName() + " ";
        if (!rawCell.hasSingleOutputPin()) {
            logger.debug(logPrefix + "(multiple output pins)");
            return false;
        }
        if (rawCell.getInputPins().size() < 1) {
            logger.debug(logPrefix + "(no input pins)");
            return false;
        }
        if (!this.stageCounts.getFootprintDefaults().containsKey(rawCell.getFootprint())) {
            logger.debug(logPrefix + "(no stage count information)");
            return false;
        }
        if (this.skipDeviatingSizes && this.stageCounts.getDeviatingSizes().containsKey(rawCell.getName())) {
            logger.debug(logPrefix + "(size with deviating cell count)");
            return false;
        }
        return true;
    }

    private List<String> getOrderedPinNames(Cell rawCell) {
        List<String> pinNames = new ArrayList<>();
        for (Pin p : rawCell.getPins()) {
            pinNames.add(p.getName());
        }
        return pinNames;
    }

    private List<String> extractInputPinNames(Cell rawCell) {
        List<String> pinNames = new ArrayList<>();
        for (Pin p : rawCell.getInputPins()) {
            pinNames.add(p.getName());
        }
        return pinNames;
    }

    private String extractOutputPinName(Cell rawCell) {
        return rawCell.getOutputPin().getName();
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

        return sizeDelayLines;
    }

    private DelayLine extractDelayLineFor(String pinName, Cell rawSize, double inputCapacitance) {
        List<DelayLine> delayLines = new ArrayList<>();
        for (Timing t : rawSize.getOutputPin().getTimings()) {
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

    private DelayLine extractDelayLine(DelayMatrix delayMatrix, double inputCapacitance) {
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
            Map<String, DelayLine> delayLinesForPin = delayLines.get(pinName);
            int stageCountForPin = stageCounts.get(pinName);
            delayParameterTriplesPerPin.put(pinName, new DelayParametersExtractor(delayLinesForPin, stageCountForPin).run());
        }

        return delayParameterTriplesPerPin;
    }
}
