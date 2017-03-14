package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public abstract class AbstractDriveOptimizer {

    protected static final Logger logger = LogManager.getLogger();

    protected List<CellInstance> cellInstances;

    public AbstractDriveOptimizer(Netlist netlist) {
        if (!netlist.isInlined()) {
            throw new Error("Cannot optimize on non-inlined netlists.");
        }
        this.cellInstances = netlist.getRootModule().getCellInstances();
    }


    protected void selectSizesFromTheoretical() {
        for (CellInstance i : this.cellInstances) {
            i.selectSizeFromTheoreticalCapacitances();
        }
    }
}
