package de.uni_potsdam.hpi.asg.drivestrength.netlist.assigncleaner;

import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.PinAssignment;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal.Direction;

public class NetlistAssignCleaner {
    private Module netlistModule;
    
    public NetlistAssignCleaner(Netlist netlist) {
        assertIsInlined(netlist);
        this.netlistModule = netlist.getRootModule();
    }
    
    public void run() {
        createConstantsIfNecessary();
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
    
    public void createConstantsIfNecessary() {
        List<Signal> obsoleteSignals = new ArrayList<>();
        Signal constantZeroWire = new Signal("constantZero", Direction.wire, 1);
        Signal constantOneWire = new Signal("constantOne", Direction.wire, 1);
        boolean hasZero = false;
        boolean hasOne = false;
        for (Signal s : netlistModule.getSignals()) {
            if (s.getDirection() == Direction.supply0) {
                hasZero = true;
                replaceSignal(s, constantZeroWire);
                obsoleteSignals.add(s);
            }
            if (s.getDirection() == Direction.supply1) {
                hasOne = true;
                replaceSignal(s, constantOneWire);
                obsoleteSignals.add(s);
            }
        }
        if (hasZero) {
            netlistModule.addSignal(constantZeroWire);
            netlistModule.addAssignConnection(new AssignConnection(Signal.getZeroInstance(), constantZeroWire, 0, 0));
        }
        if (hasOne) {
            netlistModule.addSignal(constantOneWire);
            netlistModule.addAssignConnection(new AssignConnection(Signal.getOneInstance(), constantOneWire, 0, 0));
        }
        for (Signal obsoleteSignal : obsoleteSignals) {
            netlistModule.removeSignal(obsoleteSignal);
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
