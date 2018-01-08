package de.uni_potsdam.hpi.asg.drivestrength;

import java.io.File;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import de.uni_potsdam.hpi.asg.common.iohelper.CommandlineOptions;

public class DrivestrengthCommandlineOptions extends CommandlineOptions {
    public boolean parseCmdLine(String[] args) {
        return super.parseCmdLine(args, "Usage: ASGdrivestrength");
    }

    @Option(name = "-o", metaVar = "<level>", usage = "Outputlevel: 0:nothing\n1:errors\n2:+warnings\n[3:+info]")
    private int outputlevel             = 3;

    @Option(name = "-log", metaVar = "<logfile>", usage = "Define output Logfile, default is drivestrength.log")
    private File logfile = new File("drivestrength.log");

    @Option(name = "-debug")
    private boolean debug = false;

    @Option(name = "-skipFlattener")
    private boolean skipFlattener = false;
    
    @Option(name = "-runBenchmarks")
    private boolean runBenchmarks = false;

    @Option(name = "-lib", metaVar = "<libertyfile>", usage ="Liberty Cell Library File", required = true)
    private File libertyFile;

    @Option(name = "-cellInfoJson", metaVar = "<additionalCellInfoJsonFile>", usage="JSON file containing cell stage counts, default sizes and ordered size names", required = true)
    private File additionalCellInfoJsonFile;

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

    @Option(name = "-outSdc", metaVar="<outputConstraintFile>", usage="Output file name for the constraints (sdc format)", required = false)
    private File outputConstraintFile;

    @Option(name = "-printLoadGraph", metaVar="<printLoadGraph>", usage="Enable to print load graph to the console", required = false)
    private boolean printLoadGraph = false;

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

    public File getRemoteConfigFile() {
        return remoteConfigFile;
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

    public File getAdditionalCellInfoJsonFile() {
        return additionalCellInfoJsonFile;
    }

    public boolean getPrintLoadGraph() {
        return printLoadGraph;
    }

    public File getOutputConstraintFile() {
        return outputConstraintFile;
    }
    
    public boolean isSkipFlattener() {
        return skipFlattener;
    }
}
