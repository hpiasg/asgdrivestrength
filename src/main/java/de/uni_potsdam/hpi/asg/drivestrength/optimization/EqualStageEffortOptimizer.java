package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class EqualStageEffortOptimizer extends AbstractDriveOptimizer {

    private int roundCount;
    private boolean clampToImplementableCapacitances;

    public EqualStageEffortOptimizer(Netlist netlist, int rounds, boolean clampToImplementableCapacitances) {
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
        double targetEffort = avgStageEffort();

        for (CellInstance c : this.cellInstances) {
            double loadCapacitance = c.getLoadCapacitanceTheoretical();
            for (String pinName : c.getInputPinNames()) {
                double stageEffort = calculateStageEffort(c, pinName, loadCapacitance);
                double error = stageEffort / targetEffort;
                if (error > 1) { //too much stage effort, make stronger
                    double newCapacitance = c.getInputPinTheoreticalCapacitance(pinName) * Math.min(error, 1.2);
                    c.setInputPinTheoreticalCapacitance(pinName, newCapacitance, clampToImplementableCapacitances);
                }
                if (error < 1) { //too little stage effort, make weaker
                    double newCapacitance = c.getInputPinTheoreticalCapacitance(pinName) * Math.max(error,  0.8);
                    c.setInputPinTheoreticalCapacitance(pinName, newCapacitance, clampToImplementableCapacitances);
                }
            }
        }
    }

    private double avgStageEffort() {
        double sum = 0.0;
        int count = 0;
        for (CellInstance c : this.cellInstances) {
            double loadCapacitance = c.getLoadCapacitanceTheoretical();
            for (String pinName : c.getInputPinNames()) {
                double stageEffort = calculateStageEffort(c, pinName, loadCapacitance);
                sum += stageEffort;
                count++;
            }
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
