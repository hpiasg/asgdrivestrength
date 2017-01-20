package de.uni_potsdam.hpi.asg.drivestrength.netlist.assigncleaner;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;

public class NetlistAssignCleaner {
    private Module netlistModule;
    
    public NetlistAssignCleaner(Netlist netlist) {
        assertIsInlined(netlist);
        this.netlistModule = netlist.getRootModule();
    }
    
    public void run() {
        for (AssignConnection a : netlistModule.getAssignConnections()) {
            if (a.getSourceSignal().isWire() && a.getDestinationSignal().isWire()) {
                System.out.println("wire-only assign " + a.getDestinationSignal().getName() + " = " + a.getSourceSignal().getName());
            }
        }
    }

    private void assertIsInlined(Netlist netlist) {
        if (netlist.getModules().size() != 1) {
            throw new Error("Non-inlined netlist passed to BundleSplitter. Implemented only for inlined netlists.");
        }
    }
}
