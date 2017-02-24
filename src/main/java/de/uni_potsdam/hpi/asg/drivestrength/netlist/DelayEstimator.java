package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class DelayEstimator {
    private Netlist netlist;
    private boolean useTheoreticalLoad;
    
    public DelayEstimator(Netlist netlist, boolean useTheoreticalLoad) {
        this.netlist = netlist;
        this.useTheoreticalLoad = useTheoreticalLoad;
    }
    
    public void run() {
        for (CellInstance c : this.netlist.getRootModule().getCellInstances()) {
            double loadCapacitance = this.findLoadCapacitance(c);
            for (String pinName : c.getInputPinNames()) {
                double estimatedDelay = estimateDelay(c, pinName, loadCapacitance);
                System.out.println(Cell.sortableName(c.getDefinitionName()) + "__" + pinName + "__" + c.getName() + ", " + 1000 * estimatedDelay + " ");
            }
        }
    }
    
    private double findLoadCapacitance(CellInstance c) {
        if (this.useTheoreticalLoad) {
            return c.getLoadCapacitanceTheoretical();
        }
        return c.getLoadCapacitanceSelected();
    }

    private double estimateDelay(CellInstance cellInstance, String pinName, double loadCapacitance) {
        double inputCapacitance = cellInstance.getInputPinTheoreticalCapacitance(pinName);
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
