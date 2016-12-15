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
    
    @Option(name = "-lib", metaVar = "<libertyfile>", usage ="Liberty Cell Library File", required = true)
    private File libertyFile;

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

    public File getNetlistFile() {
        return netlistFile;
    }
    
    public File getLibertyFile() {
        return libertyFile;
    }
    
    public File getWorkingdir() {
        return workingdir;
    }


}
