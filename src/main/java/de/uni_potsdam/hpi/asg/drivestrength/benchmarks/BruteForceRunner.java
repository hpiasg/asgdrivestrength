package de.uni_potsdam.hpi.asg.drivestrength.benchmarks;

import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.EnergyEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.AllSmallestOptimizer;

public class BruteForceRunner {
    private List<CellInstance> cellInstances;
    private Netlist netlist;

    public BruteForceRunner(Netlist netlist) {
        if (!netlist.isInlined()) {
            throw new Error("Cannot run brute force on non-inlined netlists.");
        }
        this.netlist = netlist;
        this.cellInstances = new ArrayList<>();
        for (CellInstance c : netlist.getRootModule().getCellInstances()) {
            if (!c.isInputDriven()) {
                this.cellInstances.add(c);
            }
        }
    }

    public void run() {
        new AllSmallestOptimizer(this.netlist).run();
        printSelectedSizes();
        printEstimates();
        while (!this.lastPermutationReached()) {
            nextNetlistSizePermutation();
            printSelectedSizes();
            printEstimates();
        }
    }

    private void printSelectedSizes() {
        for (CellInstance c : this.cellInstances) {
            System.out.print(c.getSelectedSize().getName() + ", ");
        }
    }

    private void printEstimates() {
        double energy = new EnergyEstimator(netlist, false).run();
        double delay = new DelayEstimator(netlist, false, false).run();
        System.out.println(energy + ", " + delay);
    }

    private void nextNetlistSizePermutation() {
        int index = cellInstances.size() - 1;
        while (this.hasLargestSize(cellInstances.get(index))) {
            this.selectSmallestSize(cellInstances.get(index));
            index--;
        }
        cellInstances.get(index).selectNextBiggerSizeIfPossible();
    }

    private void selectSmallestSize(CellInstance c) {
        c.selectSize(c.getDefinition().getRawSizes().get(0));
    }

    private boolean hasLargestSize(CellInstance c) {
        Cell largerSize = c.getDefinition().getNextBiggerSizeTo(c.getSelectedSize());
        return (largerSize == c.getSelectedSize());
    }

    private boolean lastPermutationReached() {
        for (CellInstance c : this.cellInstances) {
            if (!this.hasLargestSize(c)) {
                return false;
            }
        }
        return true;
    }
}
