package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;

public class LibertyCellParser {

    private static final Pattern cellNamePattern = Pattern.compile("^(\\s*)cell\\s*\\((.*)\\)\\s*");
    private static final Pattern cellFootprintPattern = Pattern.compile("^(\\s*)cell_footprint\\s*\\:\\s*(.*)\\;\\s*$");
    private static final Pattern startPinPattern = Pattern.compile("^(\\s*)pin\\s*\\((.*)\\)\\s*");

    private List<String>statements;

    public LibertyCellParser(List<String> statements) {
        this.statements = statements;
    }

    public Cell run() {
        Cell cell = new Cell();
        Matcher m = cellNamePattern.matcher(statements.get(0));
        if(m.matches()) {
            cell.setName(m.group(2));
            System.out.println("Cell " + cell.getName());
        }

        for (String statement : this.statements) {
            m = cellFootprintPattern.matcher(statement);
            if (m.matches()) {
                cell.setFootprint(m.group(2));
            }
        }

        List<List<String>> pinBlocks = new IndentBlockSeparator(statements, startPinPattern).run();

        for (List<String> pinBlock : pinBlocks) {
            cell.addPin((new LibertyPinParser(pinBlock).run()));
        }

        return cell;
    }

}
