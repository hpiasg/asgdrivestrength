package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import java.util.HashMap;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class NeighborStageEffortOptimizer extends AbstractDriveOptimizer {

    private int roundCount;
    private boolean clampToImplementableCapacitances;

    public NeighborStageEffortOptimizer(Netlist netlist, int rounds, boolean clampToImplementableCapacitances) {
        super(netlist);
        this.roundCount = rounds;
        this.clampToImplementableCapacitances = clampToImplementableCapacitances;
    }

    @Override
    protected void optimize() {
        for (int i = 0; i < this.roundCount; i++) {
            optimizeOneRound();
        }
        this.selectSizesFromTheoretical();
    }

    private void optimizeOneRound() {
        Map<CellInstance, Double> targetEfforts = determineTargetEfforts();

        for (CellInstance c : targetEfforts.keySet()) {
            double targetEffort = targetEfforts.get(c);
            double loadCapacitance = c.getLoadCapacitanceTheoretical();
            for (String pinName : c.getInputPinNames()) {
                double stageEffort = calculateStageEffort(c, pinName, loadCapacitance);
                double error = stageEffort / targetEffort;
                if (error > 1) { //too much stage effort, make stronger
                    double newCapacitance = c.getInputPinTheoreticalCapacitance(pinName) * Math.min(error, 1.05);
                    c.setInputPinTheoreticalCapacitance(pinName, newCapacitance, clampToImplementableCapacitances);
                }
                if (error < 1) { //too little stage effort, make weaker
                    double newCapacitance = c.getInputPinTheoreticalCapacitance(pinName) * Math.max(error,  0.95);
                    c.setInputPinTheoreticalCapacitance(pinName, newCapacitance, clampToImplementableCapacitances);
                }
            }
        }
    }

    private Map<CellInstance, Double> determineTargetEfforts() {
        Map<CellInstance, Double> targetEfforts = new HashMap<>();

        for (CellInstance c : this.cellInstances) {
            double targetEffort = 0;
            if (c.hasSuccessors() && c.hasPredecessors()) {
                targetEffort = avgSuccessorStageEffort(c) + avgPredecessorStageEffort(c) / 2;
            } else if (c.hasSuccessors()) {
                targetEffort = avgSuccessorStageEffort(c);
            } else if (c.hasPredecessors()) {
                targetEffort = avgPredecessorStageEffort(c);
            } else {
                logger.warn("CellInstance " + c.getName() + " has neither successors nor predecessors.");
                targetEffort = 0;
            }
            targetEfforts.put(c, targetEffort);
        }
        return targetEfforts;
    }

    @SuppressWarnings("unused")
    private double capacitanceForStageEffort(CellInstance cellInstance, String pinName, double targetEffort) {
        double loadCapacitance = cellInstance.getLoadCapacitanceTheoretical();
        int stageCount = cellInstance.getDefinition().getStageCountForPin(pinName);
        double logicalEffort = cellInstance.getDefinition().getLogicalEffortForPin(pinName);

        return loadCapacitance * logicalEffort / Math.pow(targetEffort, stageCount);
    }

    private double avgPredecessorStageEffort(CellInstance c) {
        double sum = 0.0;
        int count = 0;
        for (CellInstance predecessor : c.getPredecessors()) {
            sum += avgPinStageEffort(predecessor);
            count++;
        }
        return sum / count;
    }

    private double avgSuccessorStageEffort(CellInstance c) {
        double sum = 0.0;
        int count = 0;
        for (CellInstance successor : c.getSuccessors()) {
            sum += avgPinStageEffort(successor);
            count++;
        }
        return sum / count;
    }

    private double avgPinStageEffort(CellInstance c) {
        double sum = 0.0;
        int count = 0;
        double loadCapacitance = c.getLoadCapacitanceTheoretical();
        for (String pinName : c.getInputPinNames()) {
            double stageEffort = calculateStageEffort(c, pinName, loadCapacitance);
            sum += stageEffort;
            count++;
        }
        return sum / count;
    }

    private double calculateStageEffort(CellInstance cellInstance, String pinName, double loadCapacitance) {
        double inputCapacitance = cellInstance.getInputPinTheoreticalCapacitance(pinName);
        double electricalEffort = loadCapacitance / inputCapacitance;
        int stageCount = cellInstance.getDefinition().getStageCountForPin(pinName);
        double logicalEffort = cellInstance.getDefinition().getLogicalEffortForPin(pinName);

        /* simplifying assumption: *within a cell*, the stage efforts are equal
         * (pretend that someone chose the inner capacitances to do that for
         * *our* electrical effort */

        double stageEffort = Math.pow(electricalEffort * logicalEffort, 1.0 / stageCount);

        return stageEffort;
    }
}
