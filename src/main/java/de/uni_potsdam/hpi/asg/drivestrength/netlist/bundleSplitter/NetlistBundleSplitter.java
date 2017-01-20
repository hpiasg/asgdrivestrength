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
        createBitWiresForIO();
        useBitWiresInAssigns();
        createAssignsForIO(); //has to happen *after* useBitWiresInAssigns so it will not be modified by it
        useBitWiresInCellInstances();
        deleteReplacedBundles();
    }
    
    private void createBitWiresForIO() {
        for (Signal ioSignal : netlistModule.getIOSignals()) {
            if (ioSignal.getWidth() > 1) {
                createBitWiresForBundle(ioSignal);
            }
        }
    }
    
    private void createAssignsForIO() {
        for (Signal ioSignal : netlistModule.getIOSignals()) {
            if (ioSignal.getWidth() > 1) {
                createAssignForIOSignal(ioSignal);
            }
        }
    }
    
    private void createAssignForIOSignal(Signal ioSignal) {
        for (int bitIndex = 0; bitIndex < ioSignal.getWidth(); bitIndex++) {
            Signal internalSignal = singleBitSignalFor(ioSignal, bitIndex);
            if (ioSignal.getDirection() == Direction.input) {
                AssignConnection a = new AssignConnection(ioSignal, internalSignal, bitIndex, 0);
                netlistModule.addAssignConnection(a);
            } else {
                AssignConnection a = new AssignConnection(internalSignal, ioSignal, 0, bitIndex);
                netlistModule.addAssignConnection(a);
            }
        }
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
            Signal bitWire = new Signal(singleBitSignalName(bundle, bitIndex), Direction.wire, 1);
            netlistModule.addSignal(bitWire);
        }
    }
    
    private void useBitWiresInAssigns() {
        for (AssignConnection a : netlistModule.getAssignConnections()) {
            Signal sourceSignal = a.getSourceSignal();
            if (sourceSignal.isBundle() && (sourceSignal.isWire() || sourceSignal.isIOSignal())) {
                Signal bitSignal = singleBitSignalFor(a.getSourceSignal(), a.getSourceBitIndex());
                a.setSourceSignal(bitSignal);
                a.setSourceBitIndex(0);
            }
            Signal destinationSignal = a.getDestinationSignal();
            if (destinationSignal.isBundle() && (destinationSignal.isWire() || destinationSignal.isIOSignal())) {
                Signal bitSignal = singleBitSignalFor(a.getDestinationSignal(), a.getDestinationBitIndex());
                a.setDestinationSignal(bitSignal);
                a.setDestinationBitIndex(0);
            }
        }
    }
    
    private void useBitWiresInCellInstances() {
        for (CellInstance cellInstance : netlistModule.getCellInstances()) {
            for (PinAssignment pinAssignment : cellInstance.getPinAssignments()) {
                Signal oldSignal = pinAssignment.getSignal();
                if (!oldSignal.isBundle()) continue;
                Signal newSignal = singleBitSignalFor(oldSignal, pinAssignment.getSignalBitIndex());
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
    
    private Signal singleBitSignalFor(Signal oldSignal, int bitIndex) {
        return this.netlistModule.getSignalByName(this.singleBitSignalName(oldSignal, bitIndex));
    }
    
    private String singleBitSignalName(Signal oldSignal, int bitIndex) {
        return oldSignal.getName() + "_bit" + bitIndex;
    }
}
