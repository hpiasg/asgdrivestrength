package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.drivestrength.cells.DelayMatrix;

public class LibertyDelayMatrixParser {
    private static final Pattern matrixTypePattern = Pattern.compile("^(\\s*)(.*)\\s*\\((.*)\\)\\s*$");
    private static final Pattern inputSlewSamplesPattern = Pattern.compile("^(\\s*)index_1\\s*\\((.*)\\);\\s*$");
    private static final Pattern outputCapacitanceSamplesPattern = Pattern.compile("^(\\s*)index_2\\s*\\((.*)\\);\\s*$");
    private static final Pattern delayValuesPattern = Pattern.compile("^(\\s*)values\\s*\\((.*)\\);\\s*$");
    
    private List<String> statements;
    private DelayMatrix delayMatrix;
    
    public LibertyDelayMatrixParser(List<String> statements) {
        this.statements = statements;
    }
    
    public DelayMatrix run() {
        this.delayMatrix = new DelayMatrix();
        
        this.assertCorrectTemplateFormat();

        for (String statement: this.statements) {
            if (parseInputSlewSamplesStatement(statement)) continue;
            if (parseOutputCapacitanceSamplesStatement(statement)) continue;
            if (parseDelayValuesStatement(statement)) continue;
        }
        
        return this.delayMatrix;
    }

    private boolean parseInputSlewSamplesStatement(String statement) {
        Matcher m = inputSlewSamplesPattern.matcher(statement);
        if (!m.matches()) return false;
        String[] splitValues = m.group(2).split(",");
        for (String value : splitValues) {
            this.delayMatrix.addInputSlewSample(Float.valueOf(value.trim()));
        }
        return true;
    }

    private boolean parseOutputCapacitanceSamplesStatement(String statement) {
        Matcher m = outputCapacitanceSamplesPattern.matcher(statement);
        if (!m.matches()) return false;
        String[] splitValues = m.group(2).split(",");
        for (String value : splitValues) {
            this.delayMatrix.addOutputCapacitanceSample(Float.valueOf(value.trim()));
        }
        return true;
    }

    private boolean parseDelayValuesStatement(String statement) {
        Matcher m = delayValuesPattern.matcher(statement);
        if (!m.matches()) return false;
        String[] splitValues = m.group(2).split(",");
        for (String value : splitValues) {
            this.delayMatrix.addDelayValue(Float.valueOf(value.trim()));
        }
        return true;
    }

    private void assertCorrectTemplateFormat() {
        Matcher m = matrixTypePattern.matcher(statements.get(0));
        if (m.matches()) {
            if (!m.group(3).equals("delay_template_7x7")) {
                throw(new Error("Cannot parse Liberty timing format " 
                                + m.group(3) + " (expected: delay_template_7x7)"));
            }
        } else {
            throw(new Error());
        }
    }
}
