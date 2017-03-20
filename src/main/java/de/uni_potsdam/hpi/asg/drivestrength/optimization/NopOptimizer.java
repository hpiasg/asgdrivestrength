package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;

public class NopOptimizer extends AbstractDriveOptimizer {

    public NopOptimizer(Netlist netlist) {
        super(netlist);
    }

    @Override
    protected void optimize() {

    }
}
