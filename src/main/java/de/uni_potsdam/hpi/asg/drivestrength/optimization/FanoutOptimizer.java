package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;


public class FanoutOptimizer extends AbstractDriveOptimizer {

    public FanoutOptimizer(Netlist netlist) {
        super(netlist);
    }

    @Override
    protected void optimize() {
        for (CellInstance c : this.cellInstances) {
            int fanout = c.getLoads().size();
            c.selectSize(this.selectForFanout(fanout, c.getDefinition().getRawSizes()));
        }
    }

    private Cell selectForFanout(int fanout, List<Cell> rawSizes) {
        if (fanout == 0) {
            return rawSizes.get(0);
        }
        int smallestError = Integer.MAX_VALUE;
        Cell bestSize = rawSizes.get(0);
        for (Cell size : rawSizes) {
            int error = Math.abs(fanout - this.extractDrivestrengthFactor(size));
            if (error < smallestError) {
                smallestError = error;
                bestSize = size;
            }
        }
        return bestSize;
    }

    /* assumes raw cell size naming scheme (.*)_x  with x being 0P5, 0P25 or an integer drive strength
     * since fanout is here defined as the number of successors, it is always an integer and 0P5 and 0P25
     * are never used */
    private int extractDrivestrengthFactor(Cell cellSize) {
        String name = cellSize.getName();
        if (name.endsWith("0P5") || name.endsWith("0P25")) {
            return 0;
        }
        String[] nameSplit = name.split("_");
        return Integer.parseInt(nameSplit[nameSplit.length - 1]);
    }

}
