package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;

public class LibertyParser {
    protected static final Logger logger = LogManager.getLogger();

    private static final Pattern startCellPattern = Pattern.compile("^(\\s*)cell\\s*\\((.*)\\)\\s*");

    private List<String> statements;

    public LibertyParser(File libertyFile) {
        logger.info("Loading Liberty cell library " + libertyFile.getName());
        this.statements = this.readLibertyStatementsFromFile(libertyFile);
    }

    public List<Cell> run() {
        logger.info("Parsing Liberty cell library...");

        List<Cell> cells = new ArrayList<>();

        List<List<String>> cellBlocks = new IndentBlockSeparator(statements, startCellPattern).run();

        for (List<String> cellBlock : cellBlocks) {
            cells.add(new LibertyCellParser(cellBlock).run());
        }

        logger.info("Library contains " + cells.size() + " cells");
        return cells;
    }

    private List<String> readLibertyStatementsFromFile(File libertyFile) {
        List<String> lines = FileHelper.getInstance().readFile(libertyFile);
        assert(lines != null);
        List<String> statements = mergeMultilineStatements(lines);
        return statements;
    }


    private List<String> mergeMultilineStatements(List<String> lines) {
        List<String> statements = new ArrayList<String>();

        String statement = "";
        boolean trimNext = false;
        for (String line: lines) {
            line = line.replaceAll("\\{|\\}", "");
            line = line.replaceAll("\\\"", "");
            if (trimNext) {
                line = line.trim();
                trimNext = false;
            }
            if (line.trim().length() == 0)
                continue;
            if (line.charAt(line.length() - 1) == '\\') {
                statement += line.substring(0, line.length() - 1);
                trimNext = true;
            } else {
                statement += line;
                statements.add(statement);
                statement = "";
            }
        }

        return statements;
    }
}
