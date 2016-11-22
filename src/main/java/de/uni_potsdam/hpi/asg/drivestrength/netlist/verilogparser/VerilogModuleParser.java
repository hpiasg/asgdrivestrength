package de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal.Direction;

public class VerilogModuleParser {
    private static final Logger logger = LogManager.getLogger();
    
    private static final Pattern startmodulePattern = Pattern.compile("^\\s*module (.*) \\((.*)\\);\\s*$");

    private static final Pattern signalBundlePattern = Pattern.compile("\\s*(input|output|wire)\\s*\\[\\s*(\\d+):(\\d+)\\]\\s*(.*);");
    private static final Pattern signalSinglePattern = Pattern.compile("\\s*(input|output|wire)\\s*(.*);");
    
    private List<String>statements;
    
    private Module module;

    public VerilogModuleParser(List<String> statements) {
        this.statements = statements;
    }
    
    public Module createModule() {
        this.module = new Module();
        
        logger.info("\n");
        
        for (String statement : this.statements) {
            parseStartmoduleStatement(statement);
            parseSignalStatement(statement);
        }
        
        return this.module;
    }
    
    private void parseStartmoduleStatement(String statement) {
        Matcher m = startmodulePattern.matcher(statement);
        if(!m.matches()) {
            return;
        }
        this.module.setName(m.group(1));
        
        for(String signal : m.group(2).split(",")) {
            this.module.addInterfaceSignal(signal.trim());
        }
    }
    
    private void parseSignalStatement(String statement) {
        Matcher matcherSingle = signalSinglePattern.matcher(statement);
        Matcher matcherBundle = signalBundlePattern.matcher(statement);

        if (matcherBundle.matches()) {
            int widthLeftIndex = Integer.parseInt(matcherBundle.group(2));
            int widthRightIndex = Integer.parseInt(matcherBundle.group(3));
            int width = Math.abs(widthLeftIndex - widthRightIndex) + 1;
            registerSignals(matcherBundle.group(4), matcherBundle.group(1), width);
        } else if (matcherSingle.matches()) {
            registerSignals(matcherSingle.group(2), matcherSingle.group(1), 1);
        }
    }
    
    private void registerSignals(String namesString, String directionString, int width) {
        Direction direction = parseSignalDirection(directionString);
        List<String> names = Arrays.asList(namesString.split(","));
        for (String name : names) {
            this.module.addSignal(new Signal(name.trim(), direction, width));                
        }
    }
    
    private Direction parseSignalDirection(String directionString) {
        switch(directionString) {
            case "input":
                return Direction.input;
            case "output":
                return Direction.output;
            case "wire":
                return Direction.wire;
        }
        throw new Error("parseSignalDirection failed: " + directionString);
    }
    
}
