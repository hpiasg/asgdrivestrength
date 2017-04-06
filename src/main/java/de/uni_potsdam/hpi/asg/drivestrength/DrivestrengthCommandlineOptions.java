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

    @Option(name = "-optimizeEnergyPercentage", metaVar = "<optimizeEnergyPercentage>", usage ="Percentage for Energy in SA Optimizer (0: Minimize only Delay, 100: Minimize only Energy)", required = false)
    private int optimizeEnergyPercentage = 0;

    @Argument(metaVar = "Verilog Netlist File", required = true)
    private File netlistFile;




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
        return netlistFile;
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

}
