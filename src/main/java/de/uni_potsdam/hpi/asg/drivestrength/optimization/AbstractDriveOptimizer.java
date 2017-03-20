package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public abstract class AbstractDriveOptimizer {

    protected static final Logger logger = LogManager.getLogger();

    protected List<CellInstance> cellInstances;
    private Netlist netlist;

    public AbstractDriveOptimizer(Netlist netlist) {
        if (!netlist.isInlined()) {
            throw new Error("Cannot optimize on non-inlined netlists.");
        }
        this.netlist = netlist;
        this.cellInstances = netlist.getRootModule().getCellInstances();
    }

    public void run() {
        logger.info("Optimizing with " + this.getClass().getSimpleName() + "...");
        long startTime = System.currentTimeMillis();
        this.optimize();
        long stopTime = System.currentTimeMillis();
        logger.info("Optimization runtime: " + (stopTime - startTime) + " ms");
    }

    protected void selectSizesFromTheoretical() {
        for (CellInstance i : this.cellInstances) {
            i.selectSizeFromTheoreticalCapacitances();
        }
    }

    public Netlist getNetlist() {
        return this.netlist;
    }

    protected abstract void optimize();
}
