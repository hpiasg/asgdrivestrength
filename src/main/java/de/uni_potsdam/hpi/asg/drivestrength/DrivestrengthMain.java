package de.uni_potsdam.hpi.asg.drivestrength;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser.LibertyParser;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;

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
            if (options.parseCmdLine(args)) {
                logger = LoggerHelper.initLogger(options.getOutputlevel(),
                        options.getLogfile(), options.isDebug());
                logger.debug("Args: " + Arrays.asList(args).toString());
                WorkingdirGenerator.getInstance().create(
                        options.getWorkingdir(), "", "drivestrengthwork", null);

                status = execute();
            }
            long end = System.currentTimeMillis();
            if (logger != null) {
                logger.info("Runtime: " + LoggerHelper.formatRuntime(end - start, false));
            }
            return status;
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getLocalizedMessage());
            e.printStackTrace();
            return 1;
        }
    }

    private static int execute() {
        Netlist netlist = Netlist.newFromVerilog(options.getNetlistFile());

        logger.info("Netlist’s root module: " + netlist.getRootModule().getName());
        
//        logger.info(netlist.toVerilog());
//        
//        logger.info("\n\n\n\n\n");
//        
//        new NetlistFlattener(netlist).run();
//        
//        logger.info("\n\n\n\n\n");
//
//        logger.info(netlist.toVerilog());
        
        List<Cell> cells = new LibertyParser(options.getLibertyFile()).run();
        
        logger.info("Library contains " + cells.size() + " cells");

//        for(Cell cell: cells) {
//            System.out.println("\nCELL: " + cell.getName());
//
//            for (Pin pin : cell.getPins()) {
//                System.out.println("Pin: " + pin.getName() + " (" + pin.getDirection() + ")");
//                if (pin.getDirection() == Direction.input) {
//                    System.out.println("capacitance: " + pin.getCapacitance());
//                }
//                if (pin.hasTimings()) {
//                    for (Timing t : pin.getTimings()) {
//                        System.out.println("timing with related pin " + t.getRelatedPinName());
//                        if (t.getFallDelays() != null) {
//                            System.out.println("fall delay at 2,0: " + t.getFallDelays().getDelayAt(1, 2));
//                        }
//                        if (t.getRiseDelays() != null) {
//                            System.out.println("rise delay at 2,0: " + t.getRiseDelays().getDelayAt(1, 2));
//                        }
//                    }
//                }
//            }
//        }


        return 0;
    }
}
