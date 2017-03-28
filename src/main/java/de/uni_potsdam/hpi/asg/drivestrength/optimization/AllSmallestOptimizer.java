package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class AllSmallestOptimizer extends AbstractDriveOptimizer {

    public AllSmallestOptimizer(Netlist netlist) {
        super(netlist);
    }

    @Override
    protected void optimize() {
        for (CellInstance c : this.cellInstances) {
            for (String pinName : c.getInputPinNames()) {
                c.setInputPinTheoreticalCapacitance(pinName, 0, true);
            }
        }
        this.selectSizesFromTheoretical();
    }
}
