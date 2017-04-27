package de.uni_potsdam.hpi.asg.drivestrength;

import java.io.File;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import de.uni_potsdam.hpi.asg.common.iohelper.CommandlineOptions;

public class DrivestrengthCommandlineOptions extends CommandlineOptions {
    public boolean parseCmdLine(String[] args) {
        return super.parseCmdLine(args, "Usage: ASGdrivestrength");
    }


    @Option(name = "-o", metaVar = "<level>", usage = "Outputlevel: 0:nothing\n1:errors\n[2:+warnings]\n3:+info")
    private int outputlevel             = 2;
    @Option(name = "-log", metaVar = "<logfile>", usage = "Define output Logfile, default is drivestrength.log")
    private File logfile = new File("drivestrength.log");

    @Option(name = "-w", metaVar = "<workingdir>", usage = "Working directory. If not given, the value in configfile is used. If there is no entry, 'resynwork*' in the os default tmp dir is used.")
    private File workingdir = null;


    @Option(name = "-debug")
    private boolean debug = false;

    @Option(name = "-runBenchmarks")
    private boolean runBenchmarks = false;

    @Option(name = "-lib", metaVar = "<libertyfile>", usage ="Liberty Cell Library File", required = true)
    private File libertyFile;

    @Option(name = "-stage", metaVar = "<stageCountsFile>", usage ="Cell Stage Counts JSON File", required = true)
    private File stageCountsFile;

    @Option(name = "-defaultSizes", metaVar = "<defaultSizesFile>", usage ="Cell Default Sizes JSON File", required = true)
    private File defaultSizesFile;

    @Option(name = "-orderedSizes", metaVar = "<orderedSizesFile>", usage ="Cell Ordered Sizes JSON File", required = true)
    private File orderedSizesFile;

    @Option(name = "-remoteConfig", metaVar = "<remoteConfigFile>", usage ="Remote Config JSON File containing username, host, ...", required = false)
    private File remoteConfigFile;

    @Option(name = "-outputPinCapacitance", metaVar = "<outputPinCapacitance>", usage="Load capacitance applied to each output pin [unit: pF]. Default: 0.012")
    private double outputPinCapacitance = 0.012;

    @Option(name = "-inputDrivenMaxCIn", metaVar = "<inputDrivenMaxCIn>", usage="Limit the capacitance of cells driven by circuit input pins [unit: pF]. Default: 0.007")
    private double inputDrivenMaxCIn = 0.007;

    @Option(name = "-optimizer", metaVar = "<optimizer>", usage ="Selected Optimizer. Values are SA (default), NOP, TOP, BOT, SFL, ESE, NSE, EDM (only for all-single-stage cells), and FO", required = false)
    private String optimizer = "SA";

    @Option(name = "-optimizeEnergyPercentage", metaVar = "<optimizeEnergyPercentage>", usage ="Percentage for Energy in SA Optimizer (0: Minimize only Delay, 100: Minimize only Energy)", required = false)
    private int optimizeEnergyPercentage = 0;

    @Option(name = "-out", metaVar="<outputNetlistFile>", usage="Output file name for the optimized verilog netlist", required = false)
    private File outputNetlistFile;

    @Option(name = "-outLoadGraph", metaVar="<outputLoadGraphFile>", usage="Output file name for the capacitance load graph", required = false)
    private File outputLoadGraphFile;

    @Argument(metaVar = "Verilog Netlist Input File", required = true)
    private File netlistFileIn;


    public int getOutputlevel() {
        return outputlevel;
    }

    public File getLogfile() {
        return logfile;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isBenchmarkRun() {
        return runBenchmarks;
    }

    public File getNetlistFile() {
        return netlistFileIn;
    }

    public File getLibertyFile() {
        return libertyFile;
    }

    public File getStageCountsFile() {
        return stageCountsFile;
    }

    public File getDefaultSizesFile() {
        return defaultSizesFile;
    }

    public File getWorkingdir() {
        return workingdir;
    }

    public File getRemoteConfigFile() {
        return remoteConfigFile;
    }

    public File getOrderedSizesFile() {
        return orderedSizesFile;
    }

    public int getOptimizeEnergyPercentage() {
        return optimizeEnergyPercentage;
    }

    public File getOutputNetlistFile() {
        return outputNetlistFile;
    }

    public File getOutputLoadGraphFile() {
        return outputLoadGraphFile;
    }

    public String getOptimizer() {
        return optimizer;
    }

    public double getOutputPinCapacitance() {
        return outputPinCapacitance;
    }

    public double getInputDrivenMaxCIn() {
        return inputDrivenMaxCIn;
    }

}
