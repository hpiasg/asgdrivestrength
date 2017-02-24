package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class SelectForLoadOptimizer {

    private Netlist netlist;
    private int roundCount;
    
    public SelectForLoadOptimizer(Netlist netlist, int roundCount) {
        this.netlist = netlist;
        this.roundCount = roundCount;
    }
    
    public void run() {
        for (int i = 0; i < roundCount; i++) {
            optimizeOneRound();
        }
    }
    
    private void optimizeOneRound() {
        for (CellInstance c : this.netlist.getRootModule().getCellInstances()) {
            double load = c.getLoadCapacitanceSelected();
            c.selectFastestSizeForLoad(load);
        }
    }
}
