package de.uni_potsdam.hpi.asg.drivestrength;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper.Mode;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCellLibrary;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.aggregators.CellAggregator;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.aggregators.SizeCapacitanceMonotonizer;
import de.uni_potsdam.hpi.asg.drivestrength.benchmarks.BenchmarkRunner;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.additionalinfo.AdditionalCellInfoContainer;
import de.uni_potsdam.hpi.asg.drivestrength.cells.additionalinfo.AdditionalCellInfoParser;
import de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser.LibertyParser;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.EnergyEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.LoadGraphExporter;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.PowerEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.InputDrivenAnnotator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.LoadGraphAnnotator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.PredecessorAnnotator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistAssignCleaner;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistBundleSplitter;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistFlattener;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistInliner;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal.Direction;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser.VerilogParser;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.AllLargestOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.AllSmallestOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.EqualDelayMatrixOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.EqualStageEffortOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.FanoutOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.NeighborStageEffortOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.NopOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.SelectForLoadOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.SimulatedAnnealingOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.util.FileHelper;

public class DrivestrengthMain {
    private static Logger logger;
    private static DrivestrengthCommandlineOptions options;

    public static void main(String[] args) {
        int status = -1;
        try {
            status = main2(args);
        } catch (Exception e) {
            if (options != null && options.isDebug()) {
                e.printStackTrace();
            } else {
                System.err.println("Something really bad happend");
                status = -2;
            }
        }
        System.exit(status);
    }

    public static int main2(String[] args) {
        System.out.println("Hello World from ASGdrivestrength");

        try {
            long start = System.currentTimeMillis();
            options = new DrivestrengthCommandlineOptions();
            if (options.parseCmdLine(args)) {
                logger = LoggerHelper.initLogger(options.getOutputlevel(),
                        options.getLogfile(), options.isDebug(), Mode.cmdline);
                logger.debug("Args: " + Arrays.asList(args).toString());
                execute();
            }
            long end = System.currentTimeMillis();
            if (logger != null) {
                logger.info("Runtime: " + LoggerHelper.formatRuntime(end - start, false));
            }
            return 0;
        } catch (Error | Exception e) {
            if (options.isDebug()) {
                logger.error("An error occurred: " + e.getLocalizedMessage());
                e.printStackTrace();
            } else {
                logger.error("An error occurred: " + e.getLocalizedMessage() + " (run with -debug for stack trace)");
            }
            return 1;
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

        if(!options.isSkipFlattener()) {
            new NetlistFlattener(netlist).run();
        }
        Netlist inlinedNetlist = new NetlistInliner(netlist).run();
        new NetlistBundleSplitter(inlinedNetlist).run();
        new NetlistAssignCleaner(inlinedNetlist).run();
        new LoadGraphAnnotator(inlinedNetlist, options.getOutputPinCapacitance()).run();
        new InputDrivenAnnotator(inlinedNetlist, options.getInputDrivenMaxCIn()).run();
        new PredecessorAnnotator(inlinedNetlist).run();

        new DelayEstimator(inlinedNetlist, false, false).print();
        new EnergyEstimator(inlinedNetlist, false).print();
        new PowerEstimator(inlinedNetlist, false).print();

//        new BruteForceRunner(inlinedNetlist).run();

        optimize(inlinedNetlist);


        boolean estimateWithTheoreticalLoad = false;
        new DelayEstimator(inlinedNetlist, estimateWithTheoreticalLoad, false).print();
        new EnergyEstimator(inlinedNetlist, false).print();
        new PowerEstimator(inlinedNetlist, false).print();
        
        writeLoadGraph(inlinedNetlist);
        writeOptimizedNetlistToFile(netlist);
        writeConstraintFile(netlist);

//        boolean remoteVerbose = false;
//        boolean keepFiles = true;
//        new RemoteSimulation(inlinedNetlist, options.getRemoteConfigFile(),
//                              options.getOutputPinCapacitance(), keepFiles, remoteVerbose).run();

        return 0;
    }

    private static AggregatedCellLibrary loadCellInformation() {
        List<Cell> cells = new LibertyParser(options.getLibertyFile()).run();

        boolean skipDeviatingSizes = false;

        AdditionalCellInfoContainer additionalCellInfo = new AdditionalCellInfoParser(options.getAdditionalCellInfoJsonFile()).run();

        CellAggregator ca = new CellAggregator(cells, additionalCellInfo, skipDeviatingSizes);
        AggregatedCellLibrary aggregatedCellLibrary = ca.run();
        new SizeCapacitanceMonotonizer(aggregatedCellLibrary).run();
        return aggregatedCellLibrary;
    }

    private static void optimize(Netlist inlinedNetlist) {
        switch (options.getOptimizer()) {
        case "NOP":
            new NopOptimizer(inlinedNetlist).run();
            break;
        case "ESE":
            new EqualStageEffortOptimizer(inlinedNetlist, 100, true).run();
            break;
        case "NSE":
            new NeighborStageEffortOptimizer(inlinedNetlist, 100, true).run();
            break;
        case "SFL":
            new SelectForLoadOptimizer(inlinedNetlist, 100).run();
            break;
        case "TOP":
            new AllLargestOptimizer(inlinedNetlist).run();
            break;
        case "BOT":
            new AllSmallestOptimizer(inlinedNetlist).run();
            break;
        case "EDM":
            new EqualDelayMatrixOptimizer(inlinedNetlist).run();
            break;
        case "FO":
            new FanoutOptimizer(inlinedNetlist).run();
            break;
        case "SA":
            new SimulatedAnnealingOptimizer(inlinedNetlist, false, 1000, options.getOptimizeDelayFactor(), options.getOptimizeEnergyFactor(), options.getOptimizePowerFactor(), options.getOptimizeSAAlgorithm()).run();
            break;
        default:
            throw new Error("Specified optimizer " + options.getOptimizer() + " does not exist");
        }
    }

    private static void writeLoadGraph(Netlist inlinedNetlist) {
        boolean exportTheoreticalLoad = false;
        String loadGraphOutput = new LoadGraphExporter(inlinedNetlist, exportTheoreticalLoad).run();

        if (options.getOutputLoadGraphFile() != null) {
            FileHelper.writeStringToTextFile(loadGraphOutput, options.getOutputLoadGraphFile());
            logger.info("Wrote capacitance load graph to " + options.getOutputLoadGraphFile());
        }
        if (options.getPrintLoadGraph()) {
            logger.info("Printing capacitance load graph to console:");
            logger.info(loadGraphOutput);
        }
    }

    private static void writeOptimizedNetlistToFile(Netlist netlist) {
        if (options.getOutputNetlistFile() != null) {
            FileHelper.writeStringToTextFile(netlist.toVerilog(), options.getOutputNetlistFile());
            logger.info("Wrote optimized netlist to " + options.getOutputNetlistFile());
        } else {
            logger.info("No output file specified. Printing optimized netlist to console:");
            logger.info(netlist.toVerilog());
        }
    }

    private static void writeConstraintFile(Netlist netlist) {
        if (options.getOutputConstraintFile() == null) {
            return;
        }
        String constraintFileContent = "set sdc_version 1.8\nset_units -time ns -resistance kOhm -capacitance pF -voltage V -current mA\n";
        double outputC = options.getOutputPinCapacitance();
        for (Signal ioSignal : netlist.getRootModule().getIOSignals()) {
            if (ioSignal.getDirection() == Direction.output) {
                constraintFileContent += "set_load " + outputC + " " + ioSignal.getName() + "\n";
            }
        }
        FileHelper.writeStringToTextFile(constraintFileContent, options.getOutputConstraintFile());
        logger.info("Wrote constraint file with output pin loads to " + options.getOutputConstraintFile());
    }

}
