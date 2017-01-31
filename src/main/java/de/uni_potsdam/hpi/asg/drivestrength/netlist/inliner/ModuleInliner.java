package de.uni_potsdam.hpi.asg.drivestrength.netlist.inliner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.ModuleInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.PinAssignment;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal.Direction;

public class ModuleInliner {
    private Module sourceModule;
    private Module inlinedModule;
    private int nextInstanceId;
    private int nextNonIOSignalId;
    
    private Map<String, Signal> signalTransformation;
    Map<String, Integer> signalBitIndexTransformation;
    
    public ModuleInliner(Module sourceModule) {
        this.sourceModule = sourceModule;
        this.nextInstanceId = 0;
        this.nextNonIOSignalId = 0;
    }

    public Module run() {
        this.inlinedModule = new Module(this.sourceModule, true);
        
        for (ModuleInstance childInstance : this.sourceModule.getModuleInstances()) {
            Module inlinedChild = new ModuleInliner(childInstance.getDefinition()).run();
            
            signalTransformation = new HashMap<>();
            signalBitIndexTransformation = new HashMap<>();
            
            inlineNonIOSignalsOf(inlinedChild);
            addPinTransformationsFor(inlinedChild, childInstance);
            inlineUnconnectedIOSignalsOf(inlinedChild);
                        
            inlineAssignConnectionsOf(inlinedChild);
            inlineCellInstancesOf(inlinedChild);
        }
        
        this.inlinedModule.removeAllModuleInstances();
        return this.inlinedModule;
    }
    
    private void inlineNonIOSignalsOf(Module childDefinition) {
        for (Signal s : childDefinition.getSignals()) {
            if (!s.isIOSignal() && !s.isConstant()) {
                Signal newSignal = new Signal(s);
                newSignal.setName("inlS" + nextNonIOSignalId++);
                signalTransformation.put(s.getName(), newSignal);
                inlinedModule.addSignal(newSignal);
            }
        }
    }
    
    private void inlineUnconnectedIOSignalsOf(Module childDefinition) {
        for (Signal s : childDefinition.getSignals()) {
            if (s.isIOSignal() && getNewSignalFor(s) == null) {
                Signal newSignal = new Signal("inlS" + nextNonIOSignalId++, Direction.wire, s.getWidth());
                signalTransformation.put(s.getName(), newSignal);
                inlinedModule.addSignal(newSignal);
            }
        }
    }
    
    private void addPinTransformationsFor(Module childDefinition, ModuleInstance childInstance) {
        List<PinAssignment> pinAssignments = childInstance.getPinAssignments();
        for (PinAssignment p : pinAssignments) {
            if (p.isPositional()) {
                int pinPosition = p.getPinPosition();
                String pinName = childDefinition.getInterfaceSignals().get(pinPosition);
                signalTransformation.put(pinName, this.inlinedModule.getSignalByName(p.getSignal().getName()));
                if (p.hasSelectedBit()) {
                    signalBitIndexTransformation.put(pinName, p.getSignalBitIndex());
                }
            } else {
                signalTransformation.put(p.getPinName(), this.inlinedModule.getSignalByName(p.getSignal().getName()));
                if (p.hasSelectedBit()) {
                    signalBitIndexTransformation.put(p.getPinName(), p.getSignalBitIndex());
                }
            }
        }
    }
    
    private void inlineAssignConnectionsOf(Module childDefinition) {
        for (AssignConnection a : childDefinition.getAssignConnections()) {
            AssignConnection newAssignConnection = new AssignConnection(a);
            
            Signal sourceSignal = getNewSignalFor(a.getSourceSignal());
            newAssignConnection.setSourceSignal(sourceSignal);
            int sourceBitIndex = getNewBitIndexFor(a.getSourceSignal(), a.getSourceBitIndex());
            newAssignConnection.setSourceBitIndex(sourceBitIndex);
            
            Signal destinationSignal = getNewSignalFor(a.getDestinationSignal());
            newAssignConnection.setDestinationSignal(destinationSignal);
            int destinationBitIndex = getNewBitIndexFor(destinationSignal, a.getDestinationBitIndex());
            newAssignConnection.setDestinationBitIndex(destinationBitIndex);
            
            inlinedModule.addAssignConnection(newAssignConnection);
        }
    }
    
    private void inlineCellInstancesOf(Module childDefinition) {
        for (CellInstance childCellInstance : childDefinition.getCellInstances()) {
            String name = "inlC" + nextInstanceId++;
            AggregatedCell cellDefinition = childCellInstance.getDefinition();
            List<PinAssignment> cellPinAssignments = new ArrayList<PinAssignment>();
            for (PinAssignment a : childCellInstance.getPinAssignments()) {
                Signal newSignal = getNewSignalFor(a.getSignal());
                int signalBitIndex = getNewBitIndexFor(a.getSignal(), a.getSignalBitIndex());
                if (newSignal == null) {
                    System.out.println("newSignal null in " + childDefinition.getName() + " for oldSignal " + a.getSignal().getName() + " for pin " + a.getPinName() + " of cell " + childCellInstance.getName());
                    newSignal = makeWire(a.getSignal());
                }
                if (a.isPositional()) {
                    cellPinAssignments.add(new PinAssignment(newSignal, signalBitIndex, a.getPinPosition()));
                } else {
                    cellPinAssignments.add(new PinAssignment(newSignal, signalBitIndex, a.getPinName()));
                }
            }
            CellInstance newCellInstance = new CellInstance(name, cellDefinition, cellPinAssignments);
            newCellInstance.setAvatar(childCellInstance.getAvatarOrSelf());
            inlinedModule.addInstance(newCellInstance);
        }
    }
    
    private Signal makeWire(Signal oldSignal) {
        String wireName = oldSignal.getName() + "_toWire";
        if (!inlinedModule.hasSignalOfName(wireName)) {
            inlinedModule.addSignal(new Signal(wireName, Direction.wire, 1));
        }
        return inlinedModule.getSignalByName(wireName);
    }
    
    private Signal getNewSignalFor(Signal oldSignal) {
        if (oldSignal.isConstant()) {
            return oldSignal;
        }
        return signalTransformation.get(oldSignal.getName());
    }
    
    private int getNewBitIndexFor(Signal oldSignal, int defaultIndex) {
        if (signalBitIndexTransformation.containsKey(oldSignal.getName())) {
            return signalBitIndexTransformation.get(oldSignal.getName());
        }
        return defaultIndex;
    }
}
