package de.uni_potsdam.hpi.asg.drivestrength;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCellLibrary;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.CellAggregator;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts.StageCountsContainer;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts.StageCountsParser;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser.LibertyParser;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.bundleSplitter.NetlistBundleSplitter;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.flattener.NetlistFlattener;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.inliner.NetlistInliner;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser.VerilogParser;

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
        
        List<Cell> cells = new LibertyParser(options.getLibertyFile()).run();
        
        logger.info("Library contains " + cells.size() + " cells");
        
        StageCountsContainer stageCounts = new StageCountsParser(options.getStageCountsFile()).run();
        
        AggregatedCellLibrary aggregatedCellLibrary = new CellAggregator(cells, stageCounts).run();

        logger.info("Aggregated to " + aggregatedCellLibrary.size() + " distinct (single-output) cells");
        
//        aggregatedCellLibrary.printDelayParameterTable();
        
        
        Netlist netlist = new VerilogParser(options.getNetlistFile(), aggregatedCellLibrary).createNetlist();

        logger.info("Netlist’s root module: " + netlist.getRootModule().getName());
        
        new NetlistFlattener(netlist).run();
        Netlist inlinedNetlist = new NetlistInliner(netlist).run();

//        logger.info("inlined:\n" + inlinedNetlist.toVerilog());
//        logger.info("\n\n\n\n\n");

        new NetlistBundleSplitter(inlinedNetlist).run();
        

      logger.info("inlined:\n" + inlinedNetlist.toVerilog());
      logger.info("\n\n\n\n\n");

        return 0;
    }
    
}
