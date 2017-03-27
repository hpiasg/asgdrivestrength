package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.util.List;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.drivestrength.cells.InpinPowerContainer;

public class LibertyInpinPowerParser {
    private static final Pattern risePowerPattern = Pattern.compile("^(\\s*)rise_power\\s*\\((.*)\\)\\s*$");
    private static final Pattern fallPowerPattern = Pattern.compile("^(\\s*)fall_power\\s*\\((.*)\\)\\s*$");

    private List<String> statements;

    public LibertyInpinPowerParser(List<String> statements) {
        this.statements = statements;
    }

    public InpinPowerContainer run() {
        InpinPowerContainer powerContainer = new InpinPowerContainer();

        List<List<String>> risePowerBlocks = new IndentBlockSeparator(statements, risePowerPattern).run();
        List<List<String>> fallPowerBlocks = new IndentBlockSeparator(statements, fallPowerPattern).run();
        if (risePowerBlocks.size() > 0)
            powerContainer.setRisePower(new LibertyPowerMatrix7x1Parser(risePowerBlocks.get(0)).run());
        if (fallPowerBlocks.size() > 0)
            powerContainer.setFallPower(new LibertyPowerMatrix7x1Parser(fallPowerBlocks.get(0)).run());

        return powerContainer;
    }
}
