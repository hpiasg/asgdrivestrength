package de.uni_potsdam.hpi.asg.drivestrength.benchmarks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCellLibrary;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.InputDrivenAnnotator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.LoadGraphAnnotator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.PredecessorAnnotator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistAssignCleaner;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistBundleSplitter;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistFlattener;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.cleaning.NetlistInliner;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser.VerilogParser;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.AbstractDriveOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.AllLargestOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.EqualDelayMatrixOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.EqualStageEffortOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.NeighborStageEffortOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.SelectForLoadOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.SimulatedAnnealingOptimizer;

public class BenchmarkRunner {
    private AggregatedCellLibrary cellLibrary;

    public BenchmarkRunner(AggregatedCellLibrary cellLibrary) {
        this.cellLibrary = cellLibrary;
    }

    public void run() {
        System.out.println("Running Benchmarks...");

        String[] benchmarkNetlists = {"inc", "mod10", "count10", "bufferx", "gcd", "mult"};
        double[] benchmarkOutCs = {0.0, 0.003, 0.012, 0.1, 1.0};
        boolean[] benchmarkLimitInputs = {true, false};

        for (String netlistName : benchmarkNetlists) {
            File netlistFile = new File("netlists/benchmarks-original/" + netlistName + ".v");
            Netlist inlinedNetlist = this.loadNetlist(netlistFile);

            for (double outputC : benchmarkOutCs) {
                for (boolean limitInput : benchmarkLimitInputs) {
                    runBenchmarkEntry(inlinedNetlist, outputC, limitInput);
                }
            }
        }
    }

    private Netlist loadNetlist(File netlistFile) {
        Netlist netlist = new VerilogParser(netlistFile, this.cellLibrary, false).createNetlist();

        new NetlistFlattener(netlist).run();

        Netlist inlinedNetlist = new NetlistInliner(netlist).run();

        new NetlistBundleSplitter(inlinedNetlist).run();
        new NetlistAssignCleaner(inlinedNetlist).run();

        new PredecessorAnnotator(inlinedNetlist).run();

        return inlinedNetlist;
    }

    private void runBenchmarkEntry(Netlist netlist, double outputC, boolean limitInput) {
        System.out.println("Benchmark Entry " + netlist.getName() + ", outputC: " + outputC + ", limitInput: " + limitInput);
        Netlist netlistCopy = new Netlist(netlist);
        new LoadGraphAnnotator(netlistCopy, outputC).run();
        if (limitInput) {
            new InputDrivenAnnotator(netlistCopy).run();
        }

        List<AbstractDriveOptimizer> optimizers = new ArrayList<>();

        optimizers.add(new EqualStageEffortOptimizer(new Netlist(netlistCopy), 100, true));
        optimizers.add(new EqualStageEffortOptimizer(new Netlist(netlistCopy), 100, false));
        optimizers.add(new NeighborStageEffortOptimizer(new Netlist(netlistCopy), 100, true));
        optimizers.add(new NeighborStageEffortOptimizer(new Netlist(netlistCopy), 100, false));
        optimizers.add(new SelectForLoadOptimizer(new Netlist(netlistCopy), 100));
        optimizers.add(new AllLargestOptimizer(new Netlist(netlistCopy)));
        optimizers.add(new SimulatedAnnealingOptimizer(new Netlist(netlistCopy), false, 100));
        optimizers.add(new SimulatedAnnealingOptimizer(new Netlist(netlistCopy), true, 100));
        if (netlist.isAllSingleStage()) {
            optimizers.add(new EqualDelayMatrixOptimizer(new Netlist(netlistCopy)));
        }

        for (AbstractDriveOptimizer optimizer : optimizers) {
            optimizer.run();

            new DelayEstimator(optimizer.getNetlist(), false, false).print();

//              boolean remoteVerbose = false;
//              boolean keepFiles = false;
//              new RemoteSimulation(options.getNetlistFile(), inlinedNetlist.toVerilog(), options.getRemoteConfigFile(),
//                                   "_noslew_nowire",
//                                    outputPinCapacitance, keepFiles, remoteVerbose).run();

        }
    }
}
