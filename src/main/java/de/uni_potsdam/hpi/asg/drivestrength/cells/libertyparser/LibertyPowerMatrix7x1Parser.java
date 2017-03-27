package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.cells.PowerMatrix7x1;

public class LibertyPowerMatrix7x1Parser {

    protected static final Logger logger = LogManager.getLogger();

    private static final Pattern matrixTypePattern = Pattern.compile("^(\\s*)(.*)\\s*\\((.*)\\)\\s*$");
    private static final Pattern inputSlewSamplesPattern = Pattern.compile("^(\\s*)index_1\\s*\\((.*)\\);\\s*$");
    private static final Pattern powerValuesPattern = Pattern.compile("^(\\s*)values\\s*\\((.*)\\);\\s*$");

    private List<String> statements;
    private PowerMatrix7x1 powerMatrix;


    public LibertyPowerMatrix7x1Parser(List<String> statements) {
        this.statements = statements;
    }

    public PowerMatrix7x1 run() {
        this.powerMatrix = new PowerMatrix7x1();

        this.testCorrectTemplateFormat();

        for (String statement: this.statements) {
            if (parseInputSlewSamplesStatement(statement)) continue;
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


    private boolean parsePowerValuesStatement(String statement) {
        Matcher m = powerValuesPattern.matcher(statement);
        if (!m.matches()) return false;
        String[] splitValues = m.group(2).split(",");

        for (String value : splitValues) {
            this.powerMatrix.addPowerValue(Float.valueOf(value.trim()));
        }
        return true;
    }

    private void testCorrectTemplateFormat() {
        Matcher m = matrixTypePattern.matcher(statements.get(0));
        if (m.matches()) {
            if (!m.group(3).equals("passive_power_template_7x1")) {
                logger.debug("Cannot parse Liberty power format \""
                                + m.group(3) + "\" (expected: passive_power_template_7x1)");
            }
        } else {
            throw(new Error());
        }
    }
}
