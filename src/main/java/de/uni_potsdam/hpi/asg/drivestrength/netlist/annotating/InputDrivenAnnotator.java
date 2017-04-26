package de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal.Direction;

public class InputDrivenAnnotator {
    protected static final Logger logger = LogManager.getLogger();

    private Module module;
    private double inputDrivenMaxCIn;

    public InputDrivenAnnotator(Netlist netlist, double inputDrivenMaxCIn) {
        this.module = netlist.getRootModule();
        this.inputDrivenMaxCIn = inputDrivenMaxCIn;
    }

    public void run() {
        logger.info("Restricting input driven capacitances to max. " + this.inputDrivenMaxCIn + " pF");
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
                    cellInstance.markAsInputDriven(inputDrivenMaxCIn);
                }
            }
        }
    }

}
