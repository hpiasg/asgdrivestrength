package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.util.NumberFormatter;

public class DelayEstimator {
    protected static final Logger logger = LogManager.getLogger();

    private Netlist netlist;
    private boolean useTheoreticalLoad;
    private boolean verbose;

    public DelayEstimator(Netlist netlist, boolean useTheoreticalLoad, boolean verbose) {
        this.netlist = netlist;
        this.useTheoreticalLoad = useTheoreticalLoad;
        this.verbose = verbose;
    }

    public void print() {
        double delaySum = this.run();
        String theoreticalNote = "(from chosen sizes)";
        if (this.useTheoreticalLoad) {
            theoreticalNote = "(from theoretical sizes)";
        }
        logger.info("Estimated cell delay sum " + theoreticalNote + ": " + NumberFormatter.spacedRounded(delaySum) + " ps");
    }

    public int run() {
        double sum = 0.0;
        int count = 0;
        for (CellInstance c : this.netlist.getRootModule().getCellInstances()) {
            double loadCapacitance = this.findLoadCapacitance(c);
            for (String pinName : c.getInputPinNames()) {
                double estimatedDelay = estimateDelay(c, pinName, loadCapacitance) * 1000;
                sum += estimatedDelay;
                count++;
                if (verbose) {
                    System.out.println(Cell.sortableName(c.getDefinitionName()) + "__" + pinName + "__" + c.getName() + ", " + estimatedDelay);
                }
            }
        }
        if (verbose) {
            System.out.println("Estimated cell delay sum: " + NumberFormatter.spacedRounded(sum) + "   avg=" + sum/count);
        }
        return (int)Math.round(sum);
    }

    private double findLoadCapacitance(CellInstance c) {
        if (this.useTheoreticalLoad) {
            return c.getLoadCapacitanceTheoretical();
        }
        return c.getLoadCapacitanceSelected();
    }

    private double findOwnInputCapacitance(CellInstance cellInstance, String pinName) {
        if (this.useTheoreticalLoad) {
            return cellInstance.getInputPinTheoreticalCapacitance(pinName);
        }
        return cellInstance.getInputPinSelectedCapacitance(pinName);
    }

    public double estimateDelay(CellInstance cellInstance, String pinName, double loadCapacitance) {
        double inputCapacitance = this.findOwnInputCapacitance(cellInstance, pinName);
        double electricalEffort = loadCapacitance / inputCapacitance;
        int stageCount = cellInstance.getDefinition().getStageCountForPin(pinName);
        double logicalEffort = cellInstance.getDefinition().getLogicalEffortForPin(pinName);

        /* simplifying assumption: *within a cell*, the stage efforts are equal
         * (pretend that someone chose the inner capacitances to do that for
         * *our* electrical effort */

        double parasiticDelay = cellInstance.getDefinition().getParasiticDelayForPin(pinName);

        return stageCount * Math.pow(logicalEffort * electricalEffort, 1.0 / stageCount) + parasiticDelay;
    }
}
