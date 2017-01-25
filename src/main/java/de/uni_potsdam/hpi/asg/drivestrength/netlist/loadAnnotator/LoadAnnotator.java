package de.uni_potsdam.hpi.asg.drivestrength.netlist.loadAnnotator;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Load;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal.Direction;

public class LoadAnnotator {
    private Module module;
    private double outputPinCapacitance;
    
    public LoadAnnotator(Netlist netlist, double outputPinCapacitance) {
        this.module = netlist.getRootModule();
        this.outputPinCapacitance = outputPinCapacitance;
    }
    
    public void run() {
        this.addLoadsToCellInstances();
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
}

