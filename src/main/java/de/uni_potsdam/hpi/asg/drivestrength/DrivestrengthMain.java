package de.uni_potsdam.hpi.asg.drivestrength;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.CellAggregator;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser.LibertyParser;

public class DrivestrengthMain {
    private static Logger logger;
    private static DrivestrengthCommandlineOptions options;

    public static void main(String[] args) {
        int status = main2(args);
        System.exit(status);
    }

    public static int main2(String[] args) {
        System.out.println("Hello World from ASGdrivestrength");

        try {
            long start = System.currentTimeMillis();
            int status = -1;
            options = new DrivestrengthCommandlineOptions();
            if (options.parseCmdLine(args)) {
                logger = LoggerHelper.initLogger(options.getOutputlevel(),
                        options.getLogfile(), options.isDebug());
                logger.debug("Args: " + Arrays.asList(args).toString());
                WorkingdirGenerator.getInstance().create(
                        options.getWorkingdir(), "", "drivestrengthwork", null);

                status = execute();
            }
            long end = System.currentTimeMillis();
            if (logger != null) {
                logger.info("Runtime: " + LoggerHelper.formatRuntime(end - start, false));
            }
            return status;
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getLocalizedMessage());
            e.printStackTrace();
            return 1;
        }
    }

    private static int execute() {
//        Netlist netlist = Netlist.newFromVerilog(options.getNetlistFile());
//
//        logger.info("Netlist’s root module: " + netlist.getRootModule().getName());
//        
//        logger.info(netlist.toVerilog());
//        
//        logger.info("\n\n\n\n\n");
//        
//        new NetlistFlattener(netlist).run();
//        
//        logger.info("\n\n\n\n\n");
//
//        logger.info(netlist.toVerilog());
        
        List<Cell> cells = new LibertyParser(options.getLibertyFile()).run();
        
        logger.info("Library contains " + cells.size() + " cells");
        
        Map<String, AggregatedCell> aggregatedCells = new CellAggregator(cells).run();

        System.out.println("Aggregated to " + aggregatedCells.size() + " distinct (single-output) cells");
        
        double invLogicalEffort = aggregatedCells.get("DSC_INV").getAvgLogicalEffort();
        
        double stdevThreshold = 0.2;
        
        List<String> behavedCells = new ArrayList<String>();
        List<String> weirdCells = new ArrayList<String>();
        
        for (AggregatedCell cell : aggregatedCells.values()) {
            System.out.println("Cell " + cell.getName() + ":");
            System.out.println("  avg LE:  " + cell.getAvgLogicalEffort() / invLogicalEffort);
            System.out.println("  stdev LE: " + cell.getStdevLogicalEffort() / invLogicalEffort);
//            System.out.print("(");
//            for (double logicalEffort: cell.getAvgLogicalEffortPerCell()) {
//                System.out.format("%.3f", logicalEffort / invLogicalEffort);
//                System.out.print("  ");
//            }
//            System.out.print(")\n");
        }
        
        
        return 0;
    }
}
