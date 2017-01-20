package de.uni_potsdam.hpi.asg.drivestrength.netlist.assigncleaner;

import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.PinAssignment;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal;

public class NetlistAssignCleaner {
    private Module netlistModule;
    
    public NetlistAssignCleaner(Netlist netlist) {
        assertIsInlined(netlist);
        this.netlistModule = netlist.getRootModule();
    }
    
    public void run() {
        List<AssignConnection> assignConnections = new ArrayList<>(netlistModule.getAssignConnections());
        for (AssignConnection a : assignConnections) {
            if (a.getSourceSignal().isWire() && a.getDestinationSignal().isWire()) {
                System.out.println("wire-only assign " + a.getDestinationSignal().getName() + " = " + a.getSourceSignal().getName());
                netlistModule.removeSignal(a.getDestinationSignal());
                netlistModule.removeAssignConnection(a);
                replaceSignal(a.getDestinationSignal(), a.getSourceSignal());
            }
        }
    }
    
    private void replaceSignal(Signal oldSignal, Signal newSignal) {
        for (AssignConnection a : netlistModule.getAssignConnections()) {
            if (a.getSourceSignal() == oldSignal) {
                a.setSourceSignal(newSignal);
            }
        }
        for (CellInstance c : netlistModule.getCellInstances()) {
            for (PinAssignment a : c.getPinAssignments()) {
                if (a.getSignal() == oldSignal) {
                    a.setSignal(newSignal);
                }
            }
        }
    }

    private void assertIsInlined(Netlist netlist) {
        if (netlist.getModules().size() != 1) {
            throw new Error("Non-inlined netlist passed to BundleSplitter. Implemented only for inlined netlists.");
        }
    }
}
