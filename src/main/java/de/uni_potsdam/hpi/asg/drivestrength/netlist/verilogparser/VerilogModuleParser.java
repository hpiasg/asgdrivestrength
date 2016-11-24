package de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal.Direction;

public class VerilogModuleParser {
    private static final Logger logger = LogManager.getLogger();
    
    private static final Pattern startmodulePattern = Pattern.compile("module (.*) \\((.*)\\);\\s*$");

    private static final Pattern signalBundlePattern = Pattern.compile("(input|output|wire)\\s*\\[\\s*(\\d+):(\\d+)\\]\\s*(.*);");
    private static final Pattern signalSinglePattern = Pattern.compile("(input|output|wire)\\s*(.*);");

    private static final Pattern assignPattern = Pattern.compile("assign\\s*(.*)\\s*=\\s*(.*)\\s*;");
    private static final Pattern signalBitIndexPattern = Pattern.compile("(.*)\\[(\\d+)\\]");
    
    private List<String>statements;
    
    private Module module;

    public VerilogModuleParser(List<String> statements) {
        this.statements = statements;
    }
    
    public Module createModule() {
        this.module = new Module();
        
        for (String statement : this.statements) {
            parseStartmoduleStatement(statement);
            parseSignalStatement(statement);
            parseAssignStatement(statement);
            parseInstanceStatement(statement);
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
    
    private void parseAssignStatement(String statement) {
        Matcher assignMatcher = assignPattern.matcher(statement);
        if(!assignMatcher.matches()) return;
        
        String sourceLiteral = assignMatcher.group(2).trim();
        String destinationLiteral = assignMatcher.group(1).trim();
        Signal sourceSignal = this.module.getSignalByName(extractSignalName(sourceLiteral));
        Signal destinationSignal = this.module.getSignalByName(extractSignalName(destinationLiteral));
        int sourceBitIndex = extractBitIndex(sourceLiteral);
        int destinationBitIndex = extractBitIndex(destinationLiteral);
        AssignConnection assignConnection = new AssignConnection(
                sourceSignal, destinationSignal, sourceBitIndex, destinationBitIndex);
        this.module.addAssignConnection(assignConnection);
    }

    private String extractSignalName(String signalLiteral) {
        Matcher m = signalBitIndexPattern.matcher(signalLiteral);
        if (m.matches()) {
            logger.info(signalLiteral + " matches bitIndexPattern. group1: " + m.group(1));
            return m.group(1);            
        }
        logger.info(signalLiteral + " does not match bitIndexPattern.");
        return signalLiteral;
    }
    
    private int extractBitIndex(String signalLiteral) {
        Matcher m = signalBitIndexPattern.matcher(signalLiteral);
        if (m.matches()) {
            return Integer.parseInt(m.group(2));            
        }
        return 0;
    }
    
    
    private void parseInstanceStatement(String statement) {
        
    }
}
