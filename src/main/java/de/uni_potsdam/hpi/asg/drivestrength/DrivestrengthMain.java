package de.uni_potsdam.hpi.asg.drivestrength;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCellLibrary;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.CellAggregator;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.defaultsizes.DefaultSizesContainer;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.defaultsizes.DefaultSizesParser;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts.StageCountsContainer;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts.StageCountsParser;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser.LibertyParser;
import de.uni_potsdam.hpi.asg.drivestrength.delayfiles.DelayFileParser;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.assigncleaner.NetlistAssignCleaner;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.bundlesplitter.NetlistBundleSplitter;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.flattener.NetlistFlattener;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.inliner.NetlistInliner;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.loadAnnotator.LoadAnnotator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser.VerilogParser;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.EqualStageEffortOptimizer;

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
        DefaultSizesContainer defaultSizes = new DefaultSizesParser(options.getDefaultSizesFile()).run();
        
        AggregatedCellLibrary aggregatedCellLibrary = new CellAggregator(cells, stageCounts, defaultSizes).run();

        logger.info("Aggregated to " + aggregatedCellLibrary.size() + " distinct (single-output) cells");
        
//        aggregatedCellLibrary.printDelayParameterTable();
        
        
        Netlist netlist = new VerilogParser(options.getNetlistFile(), aggregatedCellLibrary).createNetlist();

        logger.info("Netlist’s root module: " + netlist.getRootModule().getName());

        
        new NetlistFlattener(netlist).run();
        
        Netlist inlinedNetlist = new NetlistInliner(netlist).run();
        
        new NetlistBundleSplitter(inlinedNetlist).run();
        new NetlistAssignCleaner(inlinedNetlist).run();
        

        double outputPinCapacitance = .003;
        new LoadAnnotator(inlinedNetlist, outputPinCapacitance).run();
        
        new EqualStageEffortOptimizer(inlinedNetlist, 100, true).run();

        logger.info("with adjusted strengths:\n" + inlinedNetlist.toVerilog());
        logger.info("\n\n\n\n\n");

        //logger.info(new LoadGraphExporter(inlinedNetlist).run());

        logger.info("estimated:\n");
        
        new DelayEstimator(inlinedNetlist).run();
        
        logger.info("from delayfile:\n");
        
        new DelayFileParser(new File("delayfiles/count10-optimizedESE-clamped.sdf")).run();
        
        return 0;
    }
    
}
