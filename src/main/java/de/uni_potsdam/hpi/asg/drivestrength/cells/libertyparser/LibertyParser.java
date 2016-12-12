package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;

public class LibertyParser {

    private static final Pattern indentPattern = Pattern.compile("^(\\s*)(.*)");
    private static final Pattern startCellPattern = Pattern.compile("^(\\s*)cell\\s*\\((.*)\\)\\s*");
    
    private List<String> statements;

    public LibertyParser(File libertyFile) {
        this.statements = this.readLibertyStatementsFromFile(libertyFile);
    }
    
    public List<Cell> run() {
        System.out.println("Parsing Liberty File\n");
        
        List<Cell> cells = new ArrayList<>();

        List<String> currentCellStatements = new ArrayList<String>();
        
        boolean isReadingCellBlock = false;
        int cellBlockIndent = -1;
        for (String statement : statements) {
            Matcher m = indentPattern.matcher(statement);
            if (isReadingCellBlock && m.matches()) {
                if (m.group(1).length() == cellBlockIndent) {
                    cells.add(new LibertyCellParser(currentCellStatements).run());
                    isReadingCellBlock = false;
                } else {
                    currentCellStatements.add(statement);
                }
            }
            m = startCellPattern.matcher(statement);
            if (m.matches()) {
                isReadingCellBlock = true;
                cellBlockIndent = m.group(1).length();
                currentCellStatements = new ArrayList<String>();
                currentCellStatements.add(statement);
            }
        }
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
