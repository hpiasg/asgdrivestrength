package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.aggregators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCellLibrary;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;

/**
 * As displayed in rawcell-input-capacitances.ods some multi-stage cells exhibit the behavior
 * that a stronger size does not have a higher input capacitance in the first stage (but almost the same)
 * This class annotates an annotated cell library with monotonizedSizeCapacitances so that a higher
 * desired theoretical capacitance will then actually lead to the stronger cell being selected
 */
public class SizeCapacitanceMonotonizer {
    private List<AggregatedCell> aggregatedCells;

    public SizeCapacitanceMonotonizer(AggregatedCellLibrary aggregatedCellLibrary) {
        this.aggregatedCells = aggregatedCellLibrary.getAll();
    }

    public void run() {
        for (AggregatedCell c : this.aggregatedCells) {
            List<Cell> orderedSizes = c.getRawSizes();
            c.setMonotonizedSizeCapacitances(monotonize(c.getSizeCapacitances(), orderedSizes));
        }
    }

    private Map<String, Map<String, Double>> monotonize(Map<String, Map<String, Double>> originalSizeCapacitances, List<Cell> orderedSizes) {
        Map<String, Map<String, Double>> monotonizedCapacitances = new HashMap<>();
        for (String pinName : originalSizeCapacitances.keySet()) {
            monotonizedCapacitances.put(pinName, new HashMap<>());
            double previousC = 0;
            for (Cell rawSize : orderedSizes) {
                double currentOriginalC = originalSizeCapacitances.get(pinName).get(rawSize.getName());
                if (currentOriginalC > previousC) {
                    monotonizedCapacitances.get(pinName).put(rawSize.getName(), currentOriginalC);
                    previousC = currentOriginalC;
                } else {
                    double correctedC = previousC + 0.000001;
                    monotonizedCapacitances.get(pinName).put(rawSize.getName(), correctedC);
                    previousC = correctedC;
                }
            }
        }

        return monotonizedCapacitances;
    }
}
