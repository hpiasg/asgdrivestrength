package de.uni_potsdam.hpi.asg.drivestrength.benchmarks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCellLibrary;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.EnergyEstimator;
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
import de.uni_potsdam.hpi.asg.drivestrength.optimization.AllSmallestOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.EqualDelayMatrixOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.EqualStageEffortOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.FanoutOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.NeighborStageEffortOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.NopOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.SelectForLoadOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.optimization.SimulatedAnnealingOptimizer;
import de.uni_potsdam.hpi.asg.drivestrength.remotesimulation.RemoteSimulation;
import de.uni_potsdam.hpi.asg.drivestrength.remotesimulation.RemoteSimulationResult;

public class BenchmarkRunner {
    private AggregatedCellLibrary cellLibrary;
    private String outFileName;
    private int count;
    private File remoteConfigFile;
    private long startTime;

    public BenchmarkRunner(AggregatedCellLibrary cellLibrary, File remoteConfigFile) {
        this.cellLibrary = cellLibrary;
        this.outFileName = "benchmarks-output/" + date() + ".csv";
        this.remoteConfigFile = remoteConfigFile;
    }

    private String date() {
        DateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return dfmt.format(new Date());
    }

    public void run() {
        System.out.println("Running Benchmarks...");
        this.count = 0;
        this.startTime = System.currentTimeMillis();

        //String[] benchmarkNetlists = {"inc"};
        String benchmarkSubdir = "";
        String[] benchmarkNetlists = {"single-inv", "four-inv", "loop", "fanout-chain", "adder-nand"};
//        String benchmarkSubdir = "benchmarks-original/";
//        String[] benchmarkNetlists = {"inc", "mod10", "count10", "bufferx", "gcd", "mult"};
        double[] benchmarkOutCs = {0.0, 0.003, 0.012, 0.1, 1.0};
        double[] inputDrivenMaxCIns = {0.005, 1.0};

        int combinationCount = benchmarkNetlists.length * benchmarkOutCs.length * inputDrivenMaxCIns.length;

        for (String netlistName : benchmarkNetlists) {
            File netlistFile = new File("netlists/" + benchmarkSubdir + netlistName + ".v");
            Netlist inlinedNetlist = this.loadNetlist(netlistFile);

            for (double outputC : benchmarkOutCs) {
                for (double inputDrivenMaxCIn : inputDrivenMaxCIns) {
                    try {
                        runBenchmarkEntry(inlinedNetlist, outputC, inputDrivenMaxCIn, combinationCount);
                    } catch (IOException e) {
                        throw new Error(e);
                    }
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


        return inlinedNetlist;
    }

    Map<String, AbstractDriveOptimizer> setupOptimizers(Netlist netlist, double outputC, double inputDrivenMaxCIn) {
        Map<String, AbstractDriveOptimizer> optimizers = new HashMap<>();

        Netlist netlistCopy;

        netlistCopy = copyAndReAnnotateNetlist(netlist, outputC, inputDrivenMaxCIn);
        optimizers.put("NOP", new NopOptimizer(netlistCopy));
        netlistCopy = copyAndReAnnotateNetlist(netlist, outputC, inputDrivenMaxCIn);
        optimizers.put("ESE-clamp", new EqualStageEffortOptimizer(netlistCopy, 100, true));
        netlistCopy = copyAndReAnnotateNetlist(netlist, outputC, inputDrivenMaxCIn);
        optimizers.put("ESE-free", new EqualStageEffortOptimizer(netlistCopy, 100, false));
        netlistCopy = copyAndReAnnotateNetlist(netlist, outputC, inputDrivenMaxCIn);
        optimizers.put("NSE-clamp", new NeighborStageEffortOptimizer(netlistCopy, 100, true));
        netlistCopy = copyAndReAnnotateNetlist(netlist, outputC, inputDrivenMaxCIn);
        optimizers.put("SFL", new SelectForLoadOptimizer(netlistCopy, 100));
        netlistCopy = copyAndReAnnotateNetlist(netlist, outputC, inputDrivenMaxCIn);
        optimizers.put("TOP", new AllLargestOptimizer(netlistCopy));
        netlistCopy = copyAndReAnnotateNetlist(netlist, outputC, inputDrivenMaxCIn);
        optimizers.put("BOT", new AllSmallestOptimizer(netlistCopy));
        netlistCopy = copyAndReAnnotateNetlist(netlist, outputC, inputDrivenMaxCIn);
        optimizers.put("SA-D", new SimulatedAnnealingOptimizer(netlistCopy, false, 1000, 0));
        netlistCopy = copyAndReAnnotateNetlist(netlist, outputC, inputDrivenMaxCIn);
        optimizers.put("SA-E", new SimulatedAnnealingOptimizer(netlistCopy, false, 1000, 100));
        netlistCopy = copyAndReAnnotateNetlist(netlist, outputC, inputDrivenMaxCIn);
        optimizers.put("FO", new FanoutOptimizer(netlistCopy));
        if (netlist.isAllSingleStage()) {
            netlistCopy = copyAndReAnnotateNetlist(netlist, outputC, inputDrivenMaxCIn);
            optimizers.put("EDM", new EqualDelayMatrixOptimizer(netlistCopy));
        }

        return optimizers;
    }


    private void runBenchmarkEntry(Netlist netlist, double outputC, double inputDrivenMaxCIn, int combinationCount) throws IOException {
        System.out.println("Benchmark Entry " + netlist.getName() + ", outputC: " + outputC + ", inputDrivenMaxCIn: " + inputDrivenMaxCIn);

        Map<String, AbstractDriveOptimizer> optimizers = setupOptimizers(netlist, outputC, inputDrivenMaxCIn);

        int totalcount = optimizers.size() * combinationCount;


        List<String> optimizerNamesSorted = new ArrayList<>(optimizers.keySet());
        Collections.sort(optimizerNamesSorted);
        for (String optimizerName : optimizerNamesSorted) {
            AbstractDriveOptimizer optimizer = optimizers.get(optimizerName);
            optimizer.run();

            int estimatedDelay = new DelayEstimator(optimizer.getNetlist(), false, false).run();
            double estimatedEnergy = new EnergyEstimator(optimizer.getNetlist(), false).run();

            RemoteSimulation rs = new RemoteSimulation(optimizer.getNetlist(), this.remoteConfigFile,
                                                       outputC, false, false);
            rs.run();
            RemoteSimulationResult rsResult = rs.getResult();

            String benchmarkOutput = "benchmark-entry,";
            benchmarkOutput += netlist.getName() + ",";
            benchmarkOutput += outputC + ",";
            benchmarkOutput += inputDrivenMaxCIn + ",";
            benchmarkOutput += optimizerName + ",";
            benchmarkOutput += estimatedDelay + ",";
          /*  benchmarkOutput += rsResult.getSdfDelaySum("_orig") + ",";
            //benchmarkOutput += rsResult.getSdfDelaySum("_noslew") + ",";
            benchmarkOutput += rsResult.getSdfDelaySum("_noslew_nowire") + ",";
            benchmarkOutput += rsResult.getTestbenchSuccessTime("_orig") + ",";
            //benchmarkOutput += rsResult.getTestbenchSuccessTime("_noslew") + ",";
            benchmarkOutput += rsResult.getTestbenchSuccessTime("_noslew_nowire") + ",";
            benchmarkOutput += estimatedEnergy + ",";
            benchmarkOutput += rsResult.getTestbenchEnergy();*/

            PrintWriter fileOut = new PrintWriter(new BufferedWriter(new FileWriter(outFileName, true)));
            fileOut.println(benchmarkOutput);
            fileOut.close();

            count++;
            this.printProgress(totalcount);
        }


    }

    private void printProgress(int totalcount) {
        long currentTime = System.currentTimeMillis();
        long milliseconds = currentTime - startTime;
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
        System.out.println("Benchmark progress: " + count + " of "
                        + totalcount + " (" + Math.round(100.0 * count / totalcount) + " %)"
                        + " after " + hours + "h " + minutes + "m " + seconds + "s");

    }

    private Netlist copyAndReAnnotateNetlist(Netlist originalNetlist, double outputC, double inputDrivenMaxCIn) {
        Netlist copiedNetlist = new Netlist(originalNetlist);
        new LoadGraphAnnotator(copiedNetlist, outputC).run();
        new PredecessorAnnotator(copiedNetlist).run();
        if (inputDrivenMaxCIn > 0) {
            new InputDrivenAnnotator(copiedNetlist, inputDrivenMaxCIn).run();
        }

        return copiedNetlist;
    }
}
