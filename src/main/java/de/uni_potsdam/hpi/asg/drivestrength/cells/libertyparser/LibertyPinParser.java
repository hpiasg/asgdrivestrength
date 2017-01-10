package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Pin;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Pin.Direction;

public class LibertyPinParser {

    private static final Pattern namePattern = Pattern.compile("^(\\s*)pin\\s*\\((.*)\\)\\s*");
    private static final Pattern directionPattern = Pattern.compile("^(\\s*)direction\\s*\\:\\s*(.*)\\;\\s*$");
    private static final Pattern capacitancePattern = Pattern.compile("^(\\s*)capacitance\\s*\\:\\s*(.*)\\;\\s*$");
    private static final Pattern clockPattern = Pattern.compile("^(\\s*)clock\\s*\\:\\s*(.*)\\;\\s*$");
    private static final Pattern startTimingPattern = Pattern.compile("^(\\s*)timing\\s*\\((.*)\\)\\s*");

    private final List<String>statements;
    private Pin pin;
    
    public LibertyPinParser(List<String> statements) {
        this.statements = statements;
    }

    public Pin run() {
        this.pin = new Pin();
        
        Matcher m = namePattern.matcher(statements.get(0));
        if (m.matches()) {
            pin.setName(m.group(2));
        }
        
        for (String statement : statements) {
            if (parseDirectionStatement(statement)) continue;
            if (parseCapacitanceStatement(statement)) continue;
            if (parseClockStatement(statement)) continue;
        }
        
        if (this.pin.getDirection() == Direction.output) {
            List<List<String>> timingBlocks = new IndentBlockSeparator(statements, startTimingPattern).run();
    
            for (List<String> timingBlock : timingBlocks) {
                this.pin.addTiming(new LibertyTimingParser(timingBlock).run());
            }
        }
        
        return this.pin;
    }
    
    private boolean parseDirectionStatement(String statement) {
        Matcher directionMatcher = directionPattern.matcher(statement);
        if (!directionMatcher.matches()) return false;
        
        String dirString = directionMatcher.group(2);
        
        if (dirString.equals("input")) {
            this.pin.setDirection(Direction.input);
        } else if (dirString.equals("output")) {
            this.pin.setDirection(Direction.output);
        } else if (dirString.equals("inout")) {
            this.pin.setDirection(Direction.inout);
        } else if (dirString.equals("internal")) {
            this.pin.setDirection(Direction.internal);
        } else {
            throw(new Error("Could not parse pin direction value: " + directionMatcher.group(2)));
        }
        return true;
    }
    
    private boolean parseCapacitanceStatement(String statement) {
        Matcher capacitanceMatcher = capacitancePattern.matcher(statement);
        if (!capacitanceMatcher.matches()) return false;
        this.pin.setCapacitance(Double.parseDouble(capacitanceMatcher.group(2)));
        return true;
    }
    
    private boolean parseClockStatement(String statement) {
    	Matcher clockMatcher = clockPattern.matcher(statement);
    	if (!clockMatcher.matches()) return false;
    	this.pin.markAsClockPin();
    	return true;
    }
}
