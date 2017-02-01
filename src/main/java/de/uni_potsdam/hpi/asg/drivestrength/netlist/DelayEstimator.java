package de.uni_potsdam.hpi.asg.drivestrength.netlist;

public class DelayEstimator {
    private Netlist netlist;
    
    public DelayEstimator(Netlist netlist) {
        this.netlist = netlist;
    }
    
    public void run() {
        for (CellInstance c : this.netlist.getRootModule().getCellInstances()) {
            double loadCapacitance = c.getLoadCapacitance();
            for (String pinName : c.getInputPinNames()) {
                double estimatedDelay = estimateDelay(c, pinName, loadCapacitance);
                System.out.println(c.getDefinitionName() + "__" + pinName + "__" + c.getName() + ", " + 1000 * estimatedDelay + " ");
            }
        }
    }
    

    private double estimateDelay(CellInstance cellInstance, String pinName, double loadCapacitance) {
        double inputCapacitance = cellInstance.getInputPinCapacitance(pinName);
        double electricalEffort = loadCapacitance / inputCapacitance;
        int stageCount = cellInstance.getDefinition().getStageCountForPin(pinName);
        double logicalEffort = cellInstance.getDefinition().getLogicalEffortForPin(pinName);
        
        /* simplifying assumption: *within a cell*, the stage efforts are equal
         * (pretend that someone chose the inner capacitances to do that for 
         * *our* electrical effort */
        
        double parasiticDelay = cellInstance.getDefinition().getParasiticDelayForPin(pinName);
        
        return stageCount * Math.pow(logicalEffort * electricalEffort, 1.0 / stageCount) + parasiticDelay;
    }
}
