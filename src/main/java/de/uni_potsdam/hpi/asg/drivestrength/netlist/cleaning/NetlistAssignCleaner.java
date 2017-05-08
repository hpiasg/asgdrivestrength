package de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.PinAssignment;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal.Direction;

public class NetlistAssignCleaner {
    private Module netlistModule;
    private Set<Signal> obsoleteSignals;
    
    public NetlistAssignCleaner(Netlist netlist) {
        assertIsInlined(netlist);
        this.netlistModule = netlist.getRootModule();
    }
    
    public void run() {
        obsoleteSignals = new HashSet<>();
        createConstantsIfNecessary();
        List<AssignConnection> assignConnections = new ArrayList<>(netlistModule.getAssignConnections());
        for (AssignConnection a : assignConnections) {
            if (a.getSourceSignal().isWire() && a.getDestinationSignal().isWire()) {
                netlistModule.removeSignal(a.getDestinationSignal());
                netlistModule.removeAssignConnection(a);
                replaceSignal(a.getDestinationSignal(), a.getSourceSignal());
            }
        }
        removeUnusedSignals();
    }
    
    public void createConstantsIfNecessary() {
        Signal constantZeroWire = new Signal("constantZero", Direction.wire, 1, 0);
        Signal constantOneWire = new Signal("constantOne", Direction.wire, 1, 0);
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
    
    private void removeUnusedSignals() {
        Set<String> usedSignals = new HashSet<>();
        for (AssignConnection a : netlistModule.getAssignConnections()) {
            usedSignals.add(a.getSourceSignal().getName());
            usedSignals.add(a.getDestinationSignal().getName());
        }
        for (CellInstance c : netlistModule.getCellInstances()) {
            for (PinAssignment a : c.getPinAssignments()) {
                usedSignals.add(a.getSignal().getName());
            }
        }
        for (Signal wire : netlistModule.getWires()) {
            if (!usedSignals.contains(wire.getName())) {
                obsoleteSignals.add(wire);
            }
        }
        for (Signal obsoleteSignal : obsoleteSignals) {
            netlistModule.removeSignal(obsoleteSignal);
        }
    }

    private void assertIsInlined(Netlist netlist) {
        if (netlist.getModules().size() != 1) {
            throw new Error("Non-inlined netlist passed to BundleSplitter. Implemented only for inlined netlists.");
        }
    }
}
