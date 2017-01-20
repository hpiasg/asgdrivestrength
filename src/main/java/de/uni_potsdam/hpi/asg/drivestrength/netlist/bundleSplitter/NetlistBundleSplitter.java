package de.uni_potsdam.hpi.asg.drivestrength.netlist.bundleSplitter;

import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.PinAssignment;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal.Direction;

public class NetlistBundleSplitter {
    private Module netlistModule;
    private List<Signal> obsoleteWires;
    
    public NetlistBundleSplitter(Netlist netlist) {
        assertIsInlined(netlist);
        this.netlistModule = netlist.getRootModule();
        this.obsoleteWires = new ArrayList<>();
    }
    
    public void run() {
        for (Signal wire : netlistModule.getWires()) {
            if (wire.getWidth() > 1) {
                createBitWiresForBundle(wire);
            }
        }
        for (AssignConnection a : netlistModule.getAssignConnections()) {
            if (a.getSourceSignal().isBundle() && a.getSourceSignal().getDirection() == Direction.wire) {
                Signal bitSignal = netlistModule.getSignalByName(a.getSourceSignal().getName() + "_bit" + a.getSourceBitIndex());
                a.setSourceSignal(bitSignal);
                a.setSourceBitIndex(0);
            }
            if (a.getDestinationSignal().isBundle() && a.getDestinationSignal().getDirection() == Direction.wire) {
                Signal bitSignal = netlistModule.getSignalByName(a.getDestinationSignal().getName() + "_bit" + a.getDestinationBitIndex());
                a.setDestinationSignal(bitSignal);
                a.setDestinationBitIndex(0);
            }
        }
        for (CellInstance cellInstance : netlistModule.getCellInstances()) {
            for (PinAssignment pinAssignment : cellInstance.getPinAssignments()) {
                Signal oldSignal = pinAssignment.getSignal(); 
                if (oldSignal.isBundle()) {
                    pinAssignment.setSignal(netlistModule.getSignalByName(oldSignal.getName() + "_bit" + pinAssignment.getSignalBitIndex()));
                    pinAssignment.setSignalBitIndex(-1);
                }
            }
        }
        for (Signal obsoleteWire : this.obsoleteWires) {
            this.netlistModule.removeSignal(obsoleteWire);
        }
    }
    
    private void assertIsInlined(Netlist netlist) {
        if (netlist.getModules().size() != 1) {
            throw new Error("Non-inlined netlist passed to BundleSplitter. Implemented only for inlined netlists.");
        }
    }
    
    private void createBitWiresForBundle(Signal bundle) {
        System.out.println("splitting " + bundle.getName() + " with " + bundle.getWidth() + " bits");
        
        for (int bitIndex = 0; bitIndex < bundle.getWidth(); bitIndex++) {
            Signal bitWire = new Signal(bundle.getName() + "_bit" + bitIndex, Direction.wire, 1);
            netlistModule.addSignal(bitWire);
        }
        
        this.obsoleteWires.add(bundle);
    }
}
