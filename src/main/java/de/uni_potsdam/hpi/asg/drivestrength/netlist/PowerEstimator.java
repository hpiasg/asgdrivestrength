package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class PowerEstimator {
    protected static final Logger logger = LogManager.getLogger();

    private Netlist netlist;
    private boolean verbose;

    public PowerEstimator(Netlist netlist, boolean verbose) {
        this.netlist = netlist;
        this.verbose = verbose;
    }

    public void print() {
        logger.info("Estimated Transition Power Sum: " + this.run());
    }

    public double run() {
        double sum = 0.0;
        for (CellInstance cellInstance : this.netlist.getRootModule().getCellInstances()) {
            for (String pinName : cellInstance.getInputPinNames()) {
                double cellPower = this.estimatePower(cellInstance, pinName);
                if (verbose) {
                    System.out.println("Power for " + cellInstance.getName() + " " + pinName + ": " + cellPower);
                }
                sum += cellPower;
            }
        }

        return sum;
    }

    public double estimatePower(CellInstance c, String pinName) {
        String sizeName = c.getSelectedSize().getName();
        double cellPower = c.getDefinition().getPowerFor(sizeName, pinName);
        return cellPower;
    }
}
