package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.drivestrength.cells.TimingContainer;

public class LibertyTimingParser {

    private static final Pattern relatedPinPattern = Pattern.compile("^(\\s*)related_pin\\s*\\:\\s*(.*)\\;\\s*$");
    private static final Pattern cellRisePattern = Pattern.compile("^(\\s*)cell_rise\\s*\\((.*)\\)\\s*$");
    private static final Pattern cellFallPattern = Pattern.compile("^(\\s*)cell_fall\\s*\\((.*)\\)\\s*$");
    
    private List<String> statements;
    
    public LibertyTimingParser(List<String> statements) {
        this.statements = statements;
    }
    
    public TimingContainer run() {
        TimingContainer timing = new TimingContainer();

        for (String statement : statements) {
            Matcher m = relatedPinPattern.matcher(statement);
            if (m.matches()) {
                timing.setRelatedPinName(m.group(2));
            }
        }
        
        List<List<String>> cellRiseBlocks = new IndentBlockSeparator(statements, cellRisePattern).run();
        List<List<String>> cellFallBlocks = new IndentBlockSeparator(statements, cellFallPattern).run();
        if (cellRiseBlocks.size() > 0)
            timing.setRiseDelays(new LibertyDelayMatrixParser(cellRiseBlocks.get(0)).run());
        if (cellFallBlocks.size() > 0)
            timing.setFallDelays(new LibertyDelayMatrixParser(cellFallBlocks.get(0)).run());
        
        return timing;
    }
}
