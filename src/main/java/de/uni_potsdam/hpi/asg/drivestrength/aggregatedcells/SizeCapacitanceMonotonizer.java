package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.orderedsizes.OrderedSizesContainer;

/**
 * As displayed in rawcell-input-capacitances.ods some multi-stage cells exhibit the behavior
 * that a stronger size does not have a higher input capacitance in the first stage (but almost the same)
 * This class annotates an annotated cell library with monotonizedSizeCapacitances so that a higher
 * desired theoretical capacitance will then actually lead to the stronger cell being selected
 */
public class SizeCapacitanceMonotonizer {
    private List<AggregatedCell> aggregatedCells;
    private OrderedSizesContainer allOrderedSizes;

    public SizeCapacitanceMonotonizer(AggregatedCellLibrary aggregatedCellLibrary, OrderedSizesContainer allOrderedSizes) {
        this.aggregatedCells = aggregatedCellLibrary.getAll();
        this.allOrderedSizes = allOrderedSizes;
    }

    public void run() {
        for (AggregatedCell c : this.aggregatedCells) {
            ArrayList<String> orderedSizes = allOrderedSizes.get(c.getName());
            c.setMonotonizedSizeCapacitances(monotonize(c.getSizeCapacitances(), orderedSizes));
        }
    }

    private Map<String, Map<String, Double>> monotonize(Map<String, Map<String, Double>> originalSizeCapacitances, List<String> orderedSizes) {
        Map<String, Map<String, Double>> monotonizedCapacitances = new HashMap<>();
        for (String pinName : originalSizeCapacitances.keySet()) {
            monotonizedCapacitances.put(pinName, new HashMap<>());
            double previousC = 0;
            for (String sizeName : orderedSizes) {
                double currentOriginalC = originalSizeCapacitances.get(pinName).get(sizeName);
                if (currentOriginalC > previousC) {
                    monotonizedCapacitances.get(pinName).put(sizeName, currentOriginalC);
                    previousC = currentOriginalC;
                } else {
                    double correctedC = previousC + 0.000001;
                    monotonizedCapacitances.get(pinName).put(sizeName, correctedC);
                    previousC = correctedC;
                }
            }
        }

        return monotonizedCapacitances;
    }
}
