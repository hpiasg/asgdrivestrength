package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;

public class NaiveOptimizer {
    private Netlist netlist;
    private int roundCount;
    
    public NaiveOptimizer(Netlist netlist, int rounds) {
        this.netlist = netlist;
        this.roundCount = rounds;
        assertNetlistFitness();
    }
    
    private void assertNetlistFitness() {
        if (this.netlist.getModules().size() != 1) {
            throw new Error("Cannot optimize on non-inlined netlist");
        }
    }
    
    public void run() {
        for (int i = 0; i < this.roundCount; i++) {
            optimizeOneRound();
        }
    }
    
    private void optimizeOneRound() {
        for (CellInstance c : this.netlist.getRootModule().getCellInstances()) {
            double loadCapacitance = c.getLoadCapacitance();
            for (String pinName : c.getInputPinNames()) {
                double inputCapacitance = c.getInputPinCapacitance(pinName);
                double electricalEffort = loadCapacitance / inputCapacitance;
                if (electricalEffort > 1.5) {
                    c.setInputPinCapacitance(pinName, inputCapacitance * 1.5);
                }
            }
        }
    }
}
