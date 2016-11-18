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
    
    @Option(name = "-debug")
    private boolean debug = false;
    

    @Argument(metaVar = "Verilog Netlist File", required = true)
    private File netlistfile;



    public int getOutputlevel() {
        return outputlevel;
    }

    public File getLogfile() {
        return logfile;
    }
    
    public boolean isDebug() {
        return debug;
    }

    public File getNetlistfile() {
        return netlistfile;
    }


}
