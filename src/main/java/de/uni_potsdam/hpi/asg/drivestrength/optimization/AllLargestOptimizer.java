package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class AllLargestOptimizer {

    private Netlist netlist;

    public AllLargestOptimizer(Netlist netlist) {
        this.netlist = netlist;
    }

    public void run() {
        for (CellInstance c : this.netlist.getRootModule().getCellInstances()) {
            for (String pinName : c.getInputPinNames()) {
                c.setInputPinTheoreticalCapacitance(pinName, 1000, true);
            }
        }
        for (CellInstance c : this.netlist.getRootModule().getCellInstances()) {
            c.selectSizeFromTheoreticalCapacitances();
        }
    }

}
