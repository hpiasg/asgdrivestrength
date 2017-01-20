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
    private List<Signal> replacedBundles;
    
    public NetlistBundleSplitter(Netlist netlist) {
        assertIsInlined(netlist);
        this.netlistModule = netlist.getRootModule();
        this.replacedBundles = new ArrayList<>();
    }
    
    public void run() {
        createBitWiresForBundles();
        useBitWiresInAssigns();
        useBitWiresInCellInstances();
        deleteReplacedBundles();
    }
    
    private void createBitWiresForBundles() {
        for (Signal wire : netlistModule.getWires()) {
            if (wire.getWidth() > 1) {
                createBitWiresForBundle(wire);
                this.replacedBundles.add(wire);
            }
        }
    }
    
    private void createBitWiresForBundle(Signal bundle) {        
        for (int bitIndex = 0; bitIndex < bundle.getWidth(); bitIndex++) {
            Signal bitWire = new Signal(bundle.getName() + "_bit" + bitIndex, Direction.wire, 1);
            netlistModule.addSignal(bitWire);
        }
    }
    
    private void useBitWiresInAssigns() {
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
    }
    
    private void useBitWiresInCellInstances() {
        for (CellInstance cellInstance : netlistModule.getCellInstances()) {
            for (PinAssignment pinAssignment : cellInstance.getPinAssignments()) {
                Signal oldSignal = pinAssignment.getSignal(); 
                if (!oldSignal.isBundle()) return;
                Signal newSignal = netlistModule.getSignalByName(oldSignal.getName() + "_bit" + pinAssignment.getSignalBitIndex());
                pinAssignment.setSignal(newSignal);
                pinAssignment.setSignalBitIndex(-1);
            }
        }
    }
    
    private void deleteReplacedBundles() {
        for (Signal obsoleteWire : this.replacedBundles) {
            this.netlistModule.removeSignal(obsoleteWire);
        }
    }
    
    private void assertIsInlined(Netlist netlist) {
        if (netlist.getModules().size() != 1) {
            throw new Error("Non-inlined netlist passed to BundleSplitter. Implemented only for inlined netlists.");
        }
    }
}
