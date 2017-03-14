package de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal.Direction;

public class LoadGraphAnnotator {
    protected static final Logger logger = LogManager.getLogger();

    private Module module;
    private double outputPinCapacitance;

    public LoadGraphAnnotator(Netlist netlist, double outputPinCapacitance) {
        this.module = netlist.getRootModule();
        this.outputPinCapacitance = outputPinCapacitance;
    }

    public void run() {
        logger.info("Using output pin load " + this.outputPinCapacitance + " pF");
        for (CellInstance cellInstance : module.getCellInstances()) {
            cellInstance.clearLoads();
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

