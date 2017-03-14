package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class AllLargestOptimizer extends AbstractDriveOptimizer {

    public AllLargestOptimizer(Netlist netlist) {
        super(netlist);
    }

    public void run() {
        for (CellInstance c : this.cellInstances) {
            for (String pinName : c.getInputPinNames()) {
                c.setInputPinTheoreticalCapacitance(pinName, 1000, true);
            }
        }
        this.selectSizesFromTheoretical();
    }

}
