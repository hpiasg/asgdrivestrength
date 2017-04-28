package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;
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
            c.selectSize(this.selectForFanout(fanout, c.getDefinition()));
        }
    }

    private Cell selectForFanout(int fanout, AggregatedCell aggregatedCell) {
        List<Cell> rawSizes = aggregatedCell.getRawSizes();
        if (fanout == 0) {
            return rawSizes.get(0);
        }
        int smallestError = Integer.MAX_VALUE;
        Cell bestSize = rawSizes.get(0);
        for (Cell size : rawSizes) {
            int error = Math.abs(fanout - (int)Math.floor(aggregatedCell.getSizeDrivestrengthFanoutFactors().get(size.getName())));
            if (error < smallestError) {
                smallestError = error;
                bestSize = size;
            }
        }
        return bestSize;
    }

}
