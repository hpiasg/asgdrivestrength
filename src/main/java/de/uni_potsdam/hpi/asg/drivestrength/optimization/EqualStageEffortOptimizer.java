package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;

public class EqualStageEffortOptimizer {

    private Netlist netlist;
    private int roundCount;
    
    public EqualStageEffortOptimizer(Netlist netlist, int rounds) {
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
        //System.out.println("avgVal, avgErr");
        for (int i = 0; i < this.roundCount; i++) {
            optimizeOneRound();
        }
    }
    
    private void optimizeOneRound() {
        double errorSum = 0.0;
        double errorCount = 0;
        double targetEffort = avgStageEffort();
        
        for (CellInstance c : this.netlist.getRootModule().getCellInstances()) {
            double loadCapacitance = c.getLoadCapacitance();
            for (String pinName : c.getInputPinNames()) {
                double stageEffort = stageEffort(c, pinName, loadCapacitance);
                //System.out.println(c.getName() + " " + pinName + " f=" + stageEffort);
                double error = stageEffort/targetEffort;
                errorSum += Math.abs(error - 1);
                errorCount++;
                //System.out.println(c.getName() + "_" + pinName + "," + error + "," + c.getInputPinCapacitance(pinName) + "," + loadCapacitance);
                if (error > 1) { //too much stage effort, make stronger
                    c.setInputPinCapacitance(pinName, c.getInputPinCapacitance(pinName) * Math.min(error, 1.2), false);
                }
                if (error < 1) { //too little stage effort, make weaker
                    c.setInputPinCapacitance(pinName, c.getInputPinCapacitance(pinName) * Math.max(error,  0.8), false);
                }
            }
        }
        //System.out.println(avgStageEffort() + "," + errorSum / errorCount);
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
