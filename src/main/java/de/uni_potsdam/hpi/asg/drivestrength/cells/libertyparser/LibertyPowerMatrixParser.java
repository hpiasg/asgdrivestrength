package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.drivestrength.cells.PowerMatrix;

public class LibertyPowerMatrixParser {
    private static final Pattern matrixTypePattern = Pattern.compile("^(\\s*)(.*)\\s*\\((.*)\\)\\s*$");
    private static final Pattern inputSlewSamplesPattern = Pattern.compile("^(\\s*)index_1\\s*\\((.*)\\);\\s*$");
    private static final Pattern loadCapacitanceSamplesPattern = Pattern.compile("^(\\s*)index_2\\s*\\((.*)\\);\\s*$");
    private static final Pattern powerValuesPattern = Pattern.compile("^(\\s*)values\\s*\\((.*)\\);\\s*$");

    private List<String> statements;
    private PowerMatrix powerMatrix;

    public LibertyPowerMatrixParser(List<String> statements) {
        this.statements = statements;
    }

    public PowerMatrix run() {
        this.powerMatrix = new PowerMatrix();

        this.assertCorrectTemplateFormat();

        for (String statement: this.statements) {
            if (parseInputSlewSamplesStatement(statement)) continue;
            if (parseLoadCapacitanceSamplesStatement(statement)) continue;
            if (parsePowerValuesStatement(statement)) continue;
        }

        return this.powerMatrix;
    }

    private boolean parseInputSlewSamplesStatement(String statement) {
        Matcher m = inputSlewSamplesPattern.matcher(statement);
        if (!m.matches()) return false;
        String[] splitValues = m.group(2).split(",");
        for (String value : splitValues) {
            this.powerMatrix.addInputSlewSample(Float.valueOf(value.trim()));
        }
        return true;
    }

    private boolean parseLoadCapacitanceSamplesStatement(String statement) {
        Matcher m = loadCapacitanceSamplesPattern.matcher(statement);
        if (!m.matches()) return false;
        String[] splitValues = m.group(2).split(",");
        for (String value : splitValues) {
            this.powerMatrix.addLoadCapacitanceSample(Float.valueOf(value.trim()));
        }
        return true;
    }

    private boolean parsePowerValuesStatement(String statement) {
        Matcher m = powerValuesPattern.matcher(statement);
        if (!m.matches()) return false;
        String[] splitValues = m.group(2).split(",");

        for (String value : splitValues) {
            this.powerMatrix.addPowerValue(Float.valueOf(value.trim()));
        }
        return true;
    }

    private void assertCorrectTemplateFormat() {
        Matcher m = matrixTypePattern.matcher(statements.get(0));
        if (m.matches()) {
            if (!m.group(3).equals("power_template_7x7")) {
                throw(new Error("Cannot parse Liberty power format \""
                                + m.group(3) + "\" (expected: power_template_7x7)"));
            }
        } else {
            throw(new Error());
        }
    }
}
