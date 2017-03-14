package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class NaiveOptimizer extends AbstractDriveOptimizer {
    private int roundCount;

    public NaiveOptimizer(Netlist netlist, int rounds) {
        super(netlist);
        this.roundCount = rounds;
    }

    public void run() {
        for (int i = 0; i < this.roundCount; i++) {
            optimizeOneRound();
        }
        this.selectSizesFromTheoretical();
    }

    private void optimizeOneRound() {
        for (CellInstance c : this.cellInstances) {
            double loadCapacitance = c.getLoadCapacitanceTheoretical();
            for (String pinName : c.getInputPinNames()) {
                double inputCapacitance = c.getInputPinTheoreticalCapacitance(pinName);
                double electricalEffort = loadCapacitance / inputCapacitance;
                if (electricalEffort > 1.5) {
                    c.setInputPinTheoreticalCapacitance(pinName, inputCapacitance * 1.5, false);
                }
            }
        }
    }
}
