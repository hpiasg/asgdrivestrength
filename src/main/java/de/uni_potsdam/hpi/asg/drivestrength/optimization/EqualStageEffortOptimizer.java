package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;

public class EqualStageEffortOptimizer {

    private Netlist netlist;
    private int roundCount;
    private boolean clampToImplementableCapacitances;
    
    public EqualStageEffortOptimizer(Netlist netlist, int rounds, boolean clampToImplementableCapacitances) {
        this.netlist = netlist;
        this.roundCount = rounds;
        this.clampToImplementableCapacitances = clampToImplementableCapacitances;
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
        double targetEffort = avgStageEffort();
        
        for (CellInstance c : this.netlist.getRootModule().getCellInstances()) {
            double loadCapacitance = c.getLoadCapacitance();
            for (String pinName : c.getInputPinNames()) {
                double stageEffort = stageEffort(c, pinName, loadCapacitance);
                double error = stageEffort / targetEffort;
                if (error > 1) { //too much stage effort, make stronger
                    c.setInputPinCapacitance(pinName, c.getInputPinCapacitance(pinName) * Math.min(error, 1.2), clampToImplementableCapacitances);
                }
                if (error < 1) { //too little stage effort, make weaker
                    c.setInputPinCapacitance(pinName, c.getInputPinCapacitance(pinName) * Math.max(error,  0.8), clampToImplementableCapacitances);
                }
            }
        }
    }
    
    private double avgStageEffort() {
        double sum = 0.0;
        int count = 0;
        for (CellInstance c : this.netlist.getRootModule().getCellInstances()) {
            double loadCapacitance = c.getLoadCapacitance();
            for (String pinName : c.getInputPinNames()) {
                double stageEffort = stageEffort(c, pinName, loadCapacitance);
                sum += stageEffort;
                count++;
            }
        }
        return sum / count;
    }
    
    private double stageEffort(CellInstance cellInstance, String pinName, double loadCapacitance) {
        double inputCapacitance = cellInstance.getInputPinCapacitance(pinName);
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
