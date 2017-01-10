package de.uni_potsdam.hpi.asg.drivestrength.netlist.inliner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.GateInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.ModuleInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.PinAssignment;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal;

public class NetlistInliner {

    protected static final Logger logger = LogManager.getLogger();
    private Netlist originalNetlist;
    
    public NetlistInliner(Netlist originalNetlist) {
    	this.originalNetlist = originalNetlist;
    }
    
    public Netlist run() {
    	assertNetlistIsFlat();
    	Netlist inlinedNetlist = new Netlist();
    	inlinedNetlist.addModule(createInlinedModuleRecursively(originalNetlist.getRootModule()));
    	return inlinedNetlist;
    }
    
    private void assertNetlistIsFlat() {
    	if (!this.originalNetlist.isFlat()) {
        	logger.warn("NetlistInliner called on non-flat netlist (meaning it has submodule definition instanciated more than once)");    		
    	}
    }
    
    private Module createInlinedModuleRecursively(Module currentModule) {
    	Module inlinedModule = new Module(currentModule);
        int nextInstanceId = 0;
        int nextWireId = 0;
    	for (ModuleInstance instance : currentModule.getModuleInstances()) {
    		System.out.println("inlining " + instance.getDefinition().getName() + " into " + inlinedModule.getName());
    		Module inlinedChild = createInlinedModuleRecursively(instance.getDefinition());
    		List<PinAssignment> pinAssignments = instance.getPinAssignments();
    		Map<String, Signal> signalTransformation = new HashMap<>();
    		for (Signal s : inlinedChild.getSignals()) {
    			if (!s.isIOSignal()) {
    				Signal newSignal = new Signal(s);
    				newSignal.setName("inlinedSignal" + nextWireId++);
    				signalTransformation.put(s.getName(), newSignal);
    				inlinedModule.addSignal(newSignal);
    			}
    		}
    		for (PinAssignment p : pinAssignments) {
    			if (p.isPositional()) {
    				int pinPosition = p.getPinPosition();
    				String pinName = inlinedChild.getInterfaceSignals().get(pinPosition);
    				signalTransformation.put(pinName, p.getSignal());
    				//TODO: handle bitIndices
    			} else {
    				signalTransformation.put(p.getPinName(), p.getSignal());
    				//TODO: handle bitIndices
    			}
    		}
    		for (AssignConnection c : inlinedChild.getAssignConnections()) {
    			AssignConnection newAssignConnection = new AssignConnection(c);
    			Signal sourceSignal = signalTransformation.get(c.getSourceSignal().getName());
    			newAssignConnection.setSourceSignal(sourceSignal);
    			Signal destinationSignal = signalTransformation.get(c.getDestinationSignal().getName());
    			newAssignConnection.setDestinationSignal(destinationSignal);
    			inlinedModule.addAssignConnection(newAssignConnection);
    		}
    		for (GateInstance childGateInstance : inlinedChild.getGateInstances()) {
    			String name = "inlinedGate" + nextInstanceId++;
    			AggregatedCell definition = childGateInstance.getDefinition();
				List<PinAssignment> gatePinAssignments = new ArrayList<PinAssignment>();
				for (PinAssignment a : childGateInstance.getPinAssignments()) {
					Signal newSignal = signalTransformation.get(a.getSignal().getName());
					if (a.isPositional()) {
						gatePinAssignments.add(new PinAssignment(newSignal, a.getSignalBitIndex(), a.getPinPosition()));
						//TODO: handle bit indices
					} else {
						gatePinAssignments.add(new PinAssignment(newSignal, a.getSignalBitIndex(), a.getPinName()));
						//TODO: handle bit indices
					}
				}
				GateInstance newGateInstance = new GateInstance(name, definition, gatePinAssignments);
				inlinedModule.addInstance(newGateInstance);
    		}
    	}
    	inlinedModule.removeAllModuleInstances();
    	return inlinedModule;
    }
}
