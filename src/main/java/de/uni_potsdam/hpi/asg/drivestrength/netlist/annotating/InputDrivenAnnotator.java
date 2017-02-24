package de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating;

import java.util.HashSet;
import java.util.Set;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal.Direction;

public class InputDrivenAnnotator {
    private Module module;
    
    public InputDrivenAnnotator(Netlist netlist) {
        this.module = netlist.getRootModule();
    }

    public void run() {
        Set<Signal> inputDrivenSignals = new HashSet<>();
        
        for (Signal ioSignal : module.getIOSignals()) {
            if (ioSignal.getDirection() == Direction.input) {
                inputDrivenSignals.add(ioSignal);
            }
        }
        
        for (AssignConnection a : module.getAssignConnections()) {
            if (inputDrivenSignals.contains(a.getSourceSignal())) {
                inputDrivenSignals.add(a.getDestinationSignal());
            }
        }

        for (CellInstance cellInstance : module.getCellInstances()) {
            for (String cellInputPinName : cellInstance.getInputPinNames()) {
                Signal cellInputSignal = cellInstance.getInputSignal(cellInputPinName);
                if (inputDrivenSignals.contains(cellInputSignal)) {
                    cellInstance.markAsInputDriven();
                }
            }
        }
    }

}
