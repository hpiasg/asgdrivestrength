package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.drivestrength.cells.OutpinPowerContainer;

public class LibertyOutpinPowerParser {
    private static final Pattern relatedPinPattern = Pattern.compile("^(\\s*)related_pin\\s*\\:\\s*(.*)\\;\\s*$");
    private static final Pattern risePowerPattern = Pattern.compile("^(\\s*)rise_power\\s*\\((.*)\\)\\s*$");
    private static final Pattern fallPowerPattern = Pattern.compile("^(\\s*)fall_power\\s*\\((.*)\\)\\s*$");

    private List<String> statements;

    public LibertyOutpinPowerParser(List<String> statements) {
        this.statements = statements;
    }

    public OutpinPowerContainer run() {
        OutpinPowerContainer powerContainer = new OutpinPowerContainer();

        for (String statement : statements) {
            Matcher m = relatedPinPattern.matcher(statement);
            if (m.matches()) {
                powerContainer.setRelatedPinName(m.group(2));
            }
        }

        List<List<String>> risePowerBlocks = new IndentBlockSeparator(statements, risePowerPattern).run();
        List<List<String>> fallPowerBlocks = new IndentBlockSeparator(statements, fallPowerPattern).run();
        if (risePowerBlocks.size() > 0)
            powerContainer.setRisePower(new LibertyPowerMatrix7x7Parser(risePowerBlocks.get(0)).run());
        if (fallPowerBlocks.size() > 0)
            powerContainer.setFallPower(new LibertyPowerMatrix7x7Parser(fallPowerBlocks.get(0)).run());

        return powerContainer;
    }
}
