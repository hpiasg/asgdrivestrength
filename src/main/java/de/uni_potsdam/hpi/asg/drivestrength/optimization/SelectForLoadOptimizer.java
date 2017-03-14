package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class SelectForLoadOptimizer extends AbstractDriveOptimizer {

    private int roundCount;

    public SelectForLoadOptimizer(Netlist netlist, int roundCount) {
        super(netlist);
        this.roundCount = roundCount;
    }

    @Override
    protected void optimize() {
        for (int i = 0; i < roundCount; i++) {
            optimizeOneRound();
        }
    }

    private void optimizeOneRound() {
        for (CellInstance c : this.cellInstances) {
            double load = c.getLoadCapacitanceSelected();
            c.selectFastestSizeForLoad(load);
        }
    }
}
