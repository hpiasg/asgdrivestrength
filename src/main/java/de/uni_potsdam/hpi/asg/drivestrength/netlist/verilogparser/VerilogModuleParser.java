package de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCellLibrary;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.AssignConnection;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.ModuleInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.PinAssignment;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Signal.Direction;

public class VerilogModuleParser {
    protected static final Logger logger = LogManager.getLogger();

    private static final Pattern startmodulePattern = Pattern.compile("module (.*) \\((.*)\\);\\s*$");

    private static final Pattern signalBundlePattern = Pattern.compile("(input|output|wire|supply0|supply1)\\s*\\[\\s*(\\d+):(\\d+)\\]\\s*(.*);");
    private static final Pattern signalSinglePattern = Pattern.compile("(input|output|wire|supply0|supply1)\\s*(.*);");

    private static final Pattern assignPattern = Pattern.compile("assign\\s*(.*)\\s*=\\s*(.*)\\s*;");
    private static final Pattern signalBitIndexPattern = Pattern.compile("(.*)\\[(\\d+)\\]");

    private static final Pattern instancePattern       = Pattern.compile("\\s*(.*)\\s+([_A-Za-z0-9]+)\\s+\\((.*)\\);\\s*");
    private static final Pattern mappedPositionPattern = Pattern.compile("\\.(.*)\\((.*)\\)");

    private List<String>statements;

    private Module module;
    private Netlist netlist;
    private AggregatedCellLibrary aggregatedCellLibrary;
    private boolean replaceCellsBySingleStageGates;

    public VerilogModuleParser(List<String> statements, Netlist netlist,
            AggregatedCellLibrary aggregatedCellLibrary, boolean replaceCellsBySingleStageGates) {
        this.statements = statements;
        this.netlist = netlist;
        this.aggregatedCellLibrary = aggregatedCellLibrary;
        this.replaceCellsBySingleStageGates = replaceCellsBySingleStageGates;
    }

    public Module run() {
        this.module = new Module();

        for (String statement : this.statements) {
            if (parseStartmoduleStatement(statement)) continue;
            if (parseSignalStatement(statement)) continue;
            if (parseAssignStatement(statement)) continue;
            if (parseInstanceStatement(statement)) continue;
        }

        return this.module;
    }

    private boolean parseStartmoduleStatement(String statement) {
        Matcher m = startmodulePattern.matcher(statement);
        if(!m.matches()) {
            return false;
        }
        this.module.setName(m.group(1));
        logger.debug("Parsing module " + this.module.getName());

        for(String signal : m.group(2).split(",")) {
            this.module.addInterfaceSignal(signal.trim());
        }
        return true;
    }

    private boolean parseSignalStatement(String statement) {
        Matcher matcherSingle = signalSinglePattern.matcher(statement);
        Matcher matcherBundle = signalBundlePattern.matcher(statement);

        if (matcherBundle.matches()) {
            int widthLeftIndex = Integer.parseInt(matcherBundle.group(2));
            int widthRightIndex = Integer.parseInt(matcherBundle.group(3));
            int width = Math.abs(widthLeftIndex - widthRightIndex) + 1;
            registerSignals(matcherBundle.group(4), matcherBundle.group(1), width, widthRightIndex);
        } else if (matcherSingle.matches()) {
            registerSignals(matcherSingle.group(2), matcherSingle.group(1), 1, 0);
        } else {
            return false;
        }
        return true;
    }

    private void registerSignals(String namesString, String directionString, int width, int offset) {
        Direction direction = parseSignalDirection(directionString);
        List<String> names = Arrays.asList(namesString.split(","));
        for (String name : names) {
            this.module.addSignal(new Signal(name.trim(), direction, width, offset));
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
            case "supply0":
                return Direction.supply0;
            case "supply1":
                return Direction.supply1;
        }
        throw new Error("parseSignalDirection failed: " + directionString);
    }

    private boolean parseAssignStatement(String statement) {
        Matcher assignMatcher = assignPattern.matcher(statement);
        if(!assignMatcher.matches()) return false;

        String sourceLiteral = assignMatcher.group(2).trim();
        String destinationLiteral = assignMatcher.group(1).trim();
        Signal sourceSignal = this.module.getSignalByName(extractSignalName(sourceLiteral));
        Signal destinationSignal = this.module.getSignalByName(extractSignalName(destinationLiteral));
        int sourceBitIndex = extractBitIndex(sourceLiteral);
        int destinationBitIndex = extractBitIndex(destinationLiteral);
        AssignConnection assignConnection = new AssignConnection(
                sourceSignal, destinationSignal, sourceBitIndex, destinationBitIndex);
        this.module.addAssignConnection(assignConnection);
        return true;
    }

    private String extractSignalName(String signalLiteral) {
        Matcher m = signalBitIndexPattern.matcher(signalLiteral);
        if (m.matches()) {
            return m.group(1);
        }
        return signalLiteral;
    }

    private int extractBitIndex(String signalLiteral) {
        Matcher m = signalBitIndexPattern.matcher(signalLiteral);
        if (m.matches()) {
            return Integer.parseInt(m.group(2));
        }
        return -1;
    }


    private boolean parseInstanceStatement(String statement) {
        Matcher m = instancePattern.matcher(statement);
        if (!m.matches()) return false;

        String definitionName = m.group(1).trim();
        String instanceName = m.group(2).trim();

        List<PinAssignment> pinAssignments = parsePinAssignments(m.group(3));

        try {
            Module definition = this.netlist.getModuleByName(definitionName);
            ModuleInstance instance = new ModuleInstance(instanceName, definition, pinAssignments);
            this.module.addInstance(instance);
        } catch (Error e) {
            if (this.aggregatedCellLibrary == null) {
                this.module.addInstance(new CellInstance(instanceName, definitionName, pinAssignments));
                return true;
            }
            if (this.aggregatedCellLibrary.isTieZero(definitionName)) {
                this.handleTie0(pinAssignments);
                return true;
            }
            if (this.aggregatedCellLibrary.isTieOne(definitionName)) {
                this.handleTie1(pinAssignments);
                return true;
            }
            if (this.replaceCellsBySingleStageGates) {
                AggregatedCell definition = this.aggregatedCellLibrary.getSingleStageCellByCellName(definitionName);
                this.replacePinNamesForSingleStageGate(pinAssignments, definition);
                this.module.addInstance(new CellInstance(instanceName, definition, pinAssignments));
                return true;
            }
            AggregatedCell definition = this.aggregatedCellLibrary.getByCellName(definitionName);
            this.module.addInstance(new CellInstance(instanceName, definition, pinAssignments));
        }
        return true;
    }

    private void handleTie0(List<PinAssignment> pinAssignments) {
        AssignConnection a = new AssignConnection(Signal.getZeroInstance(), pinAssignments.get(0).getSignal(), 0, 0);
        this.module.addAssignConnection(a);
    }

    private void handleTie1(List<PinAssignment> pinAssignments) {
        AssignConnection a = new AssignConnection(Signal.getOneInstance(), pinAssignments.get(0).getSignal(), 0, 0);
        this.module.addAssignConnection(a);
    }

    private List<PinAssignment> parsePinAssignments(String pinAssignmentsLiteral) {
        List<PinAssignment> pinAssignments = new ArrayList<>();

        List<String> pinAssignmentLiterals = Arrays.asList(pinAssignmentsLiteral.trim().split("\\s*\\,\\s*"));

        int pinPosition = 0;
        for (String pinAssignmentLiteral : pinAssignmentLiterals) {
            Matcher mappedMatcher = mappedPositionPattern.matcher(pinAssignmentLiteral);
            if (mappedMatcher.matches()) {
                //mapped
                String pinName = mappedMatcher.group(1);
                String signalLiteral = mappedMatcher.group(2).trim();
                int bitIndex = extractBitIndex(signalLiteral);
                signalLiteral = signalLiteral.replaceAll("\\[.*\\]", "");
                String signalName = extractSignalName(signalLiteral);
                Signal connectedSignal = this.module.getSignalByName(signalName);
                pinAssignments.add(new PinAssignment(connectedSignal, bitIndex, pinName));
            } else {
                //positional
                String signalName = extractSignalName(pinAssignmentLiteral);
                int bitIndex = extractBitIndex(pinAssignmentLiteral);
                Signal connectedSignal = this.module.getSignalByName(signalName);
                pinAssignments.add(new PinAssignment(connectedSignal, bitIndex, pinPosition));
                pinPosition++;
            }
        }
        return pinAssignments;
    }

    private void replacePinNamesForSingleStageGate(List<PinAssignment> pinAssignments, AggregatedCell cellDefinition) {
        int replacedIndex = 0;
        List<String> newInputPinNames = cellDefinition.getInputPinNames();
        for (PinAssignment pinAssignment : pinAssignments) {
            if (pinAssignment.isPositional()) continue;
            if (pinAssignment.getPinName().equals(cellDefinition.getOutputPinName())) continue;
            pinAssignment.setPinName(newInputPinNames.get(replacedIndex));
            replacedIndex++;
        }
    }
}
