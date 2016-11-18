package de.uni_potsdam.hpi.asg.drivestrength;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.WorkingdirGenerator;

import de.uni_potsdam.hpi.asg.drivestrength.verilogparser.VerilogParser;

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
            if(options.parseCmdLine(args)) {
                logger = LoggerHelper.initLogger(options.getOutputlevel(), options.getLogfile(), options.isDebug());
                logger.debug("Args: " + Arrays.asList(args).toString());
                WorkingdirGenerator.getInstance().create(options.getWorkingdir(), "", "drivestrengthwork", null);
                
                status = execute();
            }
            long end = System.currentTimeMillis();
            if(logger != null) {
                logger.info("Runtime: " + LoggerHelper.formatRuntime(end - start, false));
            }
            return status;
        } catch(Exception e) {
            System.out.println("An error occurred: " + e.getLocalizedMessage());
            e.printStackTrace();
            return 1;
        }
    }
    

    private static int execute() {
    	logger.info("Loading verilog netlist");
        VerilogParser vparser = new VerilogParser();
        if(!vparser.parseVerilogStructure(options.getNetlistfile())) {
            return 1;
        }
        
        logger.info("Verilog netlist parsed. Root module name: " +
                    vparser.getRootModule().getModulename());

    	return 0;
    }
}
