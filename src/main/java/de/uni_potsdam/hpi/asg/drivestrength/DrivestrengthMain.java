package de.uni_potsdam.hpi.asg.drivestrength;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCellLibrary;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.CellAggregator;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.SizeCapacitanceMonotonizer;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.defaultsizes.DefaultSizesContainer;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.defaultsizes.DefaultSizesParser;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.orderedsizes.OrderedSizesContainer;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.orderedsizes.OrderedSizesParser;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts.StageCountsContainer;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.stagecounts.StageCountsParser;
import de.uni_potsdam.hpi.asg.drivestrength.benchmarks.BenchmarkRunner;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser.LibertyParser;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.LoadGraphExporter;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.InputDrivenAnnotator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.LoadGraphAnnotator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.PredecessorAnnotator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistAssignCleaner;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistBundleSplitter;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistFlattener;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistInliner;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser.VerilogParser;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.EqualStageEffortOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.remotesimulation.RemoteSimulation;

public class DrivestrengthMain {
    private static Logger logger;
    private static DrivestrengthCommandlineOptions options;

    public static void main(String[] args) {
        System.out.println("Hello World from ASGdrivestrength");

        try {
            long start = System.currentTimeMillis();
            options = new DrivestrengthCommandlineOptions();
            if (options.parseCmdLine(args)) {
                logger = LoggerHelper.initLogger(options.getOutputlevel(),
                        options.getLogfile(), options.isDebug());
                logger.debug("Args: " + Arrays.asList(args).toString());
                WorkingdirGenerator.getInstance().create(
                        options.getWorkingdir(), "", "drivestrengthwork", null);

                execute();
            }
            long end = System.currentTimeMillis();
            if (logger != null) {
                logger.info("Runtime: " + LoggerHelper.formatRuntime(end - start, false));
            }
            System.exit(0);
        } catch (Error | Exception e) {
            System.out.println("An error occurred: " + e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static int execute() {
        AggregatedCellLibrary cellLibrary = loadCellInformation();

        if (options.isBenchmarkRun()) {
            new BenchmarkRunner(cellLibrary, options.getRemoteConfigFile()).run();
            return 0;
        }

        boolean replaceBySingleStageCells = false; //Will lead to non-functional netlist, exists just to analyze our algorithm behavior
        Netlist netlist = new VerilogParser(options.getNetlistFile(), cellLibrary, replaceBySingleStageCells).createNetlist();

        new NetlistFlattener(netlist).run();

        Netlist inlinedNetlist = new NetlistInliner(netlist).run();

        new NetlistBundleSplitter(inlinedNetlist).run();
        new NetlistAssignCleaner(inlinedNetlist).run();
        new PredecessorAnnotator(inlinedNetlist).run();

        double outputPinCapacitance = 0.03;
        new LoadGraphAnnotator(inlinedNetlist, outputPinCapacitance).run();
        new InputDrivenAnnotator(inlinedNetlist).run();

        new DelayEstimator(inlinedNetlist, false, false).print();

        boolean clampToImplementableCapacitances = true;
        new EqualStageEffortOptimizer(inlinedNetlist, 100, clampToImplementableCapacitances).run();
        //new NeighborStageEffortOptimizer(inlinedNetlist, 100, clampToImplementableCapacitances).run();
        //new SelectForLoadOptimizer(inlinedNetlist, 100).run();
        //new AllLargestOptimizer(inlinedNetlist).run();
        //new SimulatedAnnealingOptimizer(inlinedNetlist, 100).run();
        //new EqualDelayMatrixOptimizer(inlinedNetlist).run();

        boolean exportTheoreticalLoad = false;
        new LoadGraphExporter(inlinedNetlist, exportTheoreticalLoad).run();

        boolean estimateWithTheoreticalLoad = false;
        new DelayEstimator(inlinedNetlist, estimateWithTheoreticalLoad, false).print();

        boolean remoteVerbose = false;
        boolean keepFiles = true;
        new RemoteSimulation(options.getNetlistFile(), inlinedNetlist.toVerilog(), options.getRemoteConfigFile(),
                              outputPinCapacitance, keepFiles, remoteVerbose).run();

        return 0;
    }

    private static AggregatedCellLibrary loadCellInformation() {
        List<Cell> cells = new LibertyParser(options.getLibertyFile()).run();

        StageCountsContainer stageCounts = new StageCountsParser(options.getStageCountsFile()).run();
        DefaultSizesContainer defaultSizes = new DefaultSizesParser(options.getDefaultSizesFile()).run();
        OrderedSizesContainer orderedSizes = new OrderedSizesParser(options.getOrderedSizesFile()).run();

        boolean skipDeviatingSizes = true;
        CellAggregator ca = new CellAggregator(cells, stageCounts, defaultSizes, orderedSizes, skipDeviatingSizes);
        AggregatedCellLibrary aggregatedCellLibrary = ca.run();
        new SizeCapacitanceMonotonizer(aggregatedCellLibrary, orderedSizes).run();
        return aggregatedCellLibrary;
    }

}
