package de.uni_potsdam.hpi.asg.drivestrength.netlist.loadAnnotator;

import java.util.HashSet;
import java.util.Set;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Load;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal.Direction;

public class LoadGraphAnnotator {
    private Module module;
    private double outputPinCapacitance;
    
    public LoadGraphAnnotator(Netlist netlist, double outputPinCapacitance) {
        this.module = netlist.getRootModule();
        this.outputPinCapacitance = outputPinCapacitance;
    }
    
    public void run() {
        this.addLoadsToCellInstances();
        this.markInputDrivenInstances();
    }
    
    private void addLoadsToCellInstances() {
        for (CellInstance cellInstance : module.getCellInstances()) {
            Signal signal = cellInstance.getOutputSignal();
            for (CellInstance c : module.getCellInstances()) {
                if (c.isInputSignal(signal)) {
                    cellInstance.addLoad(new Load(c, c.pinNameForConnectedSignal(signal)));
                }
            }
            for (AssignConnection a : module.getAssignConnections()) {
                if (a.getSourceSignal() == signal && a.getDestinationSignal().getDirection() == Direction.output) {
                    cellInstance.addLoad(new Load(this.outputPinCapacitance));
                }
            }
            for (Signal ioSignal : module.getIOSignals()) {
                if (ioSignal == signal) {
                    cellInstance.addLoad(new Load(this.outputPinCapacitance));
                }
            }
        }
    }
    
    private void markInputDrivenInstances() {
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

