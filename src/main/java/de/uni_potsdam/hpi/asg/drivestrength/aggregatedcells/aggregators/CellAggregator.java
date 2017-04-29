package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.aggregators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCellLibrary;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Pin;
import de.uni_potsdam.hpi.asg.drivestrength.cells.additionalinfo.AdditionalCellInfoContainer;

public class CellAggregator {
    protected static final Logger logger = LogManager.getLogger();

    private final List<Cell> rawCells;
    private Map<String, AggregatedCell> aggregatedCells;
    private AdditionalCellInfoContainer additionalCellInfo;
    private boolean skipDeviatingSizes;

    // We use values for input slew = 0.0161238 ns, as it is closest to the 0.0181584ns used in cell pdfs
    private final int inputSlewIndex = 0;

    public CellAggregator(List<Cell> rawCells, AdditionalCellInfoContainer additionalCellInfo, boolean skipDeviatingSizes) {
        this.rawCells = rawCells;
        this.additionalCellInfo = additionalCellInfo;
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
            aggregatedCell.setDefaultSizeName(this.findDefaultSize(aggregatedCell.getName(), rawSizes));
            aggregatedCell.setSizeDrivestrengthFanoutFactors(this.findCellFanoutFactorsFor(aggregatedCell.getName(), rawSizes));
            aggregatedCell.setOrderedRawSizes(this.orderRawSizes(aggregatedCell.getName(), rawSizes));

            new CellPowerAggregator(aggregatedCell, this.inputSlewIndex).run();
            new CellDelayAggregator(aggregatedCell, this.additionalCellInfo, this.inputSlewIndex).run();
        }

        logger.info("Aggregated to " + aggregatedCells.size() + " distinct (single-output) cells");

        return new AggregatedCellLibrary(aggregatedCells);
    }

    private List<Cell> orderRawSizes(String footprint, List<Cell> rawSizes) {
        List<Cell> rawSizesOrdered = new ArrayList<>(rawSizes);
        Collections.sort(rawSizesOrdered, (a,b) -> this.compareRawSizes(a, b));
        return rawSizesOrdered;
    }

    private int compareRawSizes(Cell a, Cell b) {
        double aSize = this.additionalCellInfo.getDrivestrengthFanoutFactorFor(a.getName());
        double bSize = this.additionalCellInfo.getDrivestrengthFanoutFactorFor(b.getName());
        return aSize < bSize ? -1 : aSize == bSize ? 0 : 1;
    }

    private String findDefaultSize(String footprint, List<Cell> rawSizes) {
        for (Cell rawSize : rawSizes) {
            double fanoutFactor = this.additionalCellInfo.getDrivestrengthFanoutFactorFor(rawSize.getName());
            if (Math.abs(fanoutFactor - 1.0) < 0.000001) {
                return rawSize.getName();
            }
        }
        throw new Error("Could not find default (1x) size in additional cell info for " + footprint);
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
        if (!this.additionalCellInfo.getDefaultStageCounts().containsKey(rawCell.getFootprint())) {
            logger.debug(logPrefix + "(no stage count information)");
            return false;
        }
        if (this.skipDeviatingSizes && this.isDeviatingSize(rawCell.getName())) {
            logger.debug(logPrefix + "(size with deviating cell count)");
            return false;
        }
        return true;
    }

    private boolean isDeviatingSize(String rawCellName) {
        return this.additionalCellInfo.listDeviatingSizes().contains(rawCellName);
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

    private Map<String, Double> findCellFanoutFactorsFor(String cellFootprint, List<Cell> rawSizes) {
        Map<String, Double> fanoutFactors = new HashMap<>();
        for (Cell rawSize : rawSizes) {
            fanoutFactors.put(rawSize.getName(), additionalCellInfo.getDrivestrengthFanoutFactorFor(rawSize.getName()));
        }
        return fanoutFactors;
    }

}
