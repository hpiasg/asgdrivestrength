package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;

public class LibertyParser {
    private List<String> statements;

    public LibertyParser(File libertyFile) {
        this.statements = this.readLibertyStatementsFromFile(libertyFile);
    }
    
    public List<Cell> run() {
        System.out.println("Parsing Liberty File\n");
        List<Cell> cells = new ArrayList<>();
        for (String statement : statements) {
            System.out.println("Statement: " + statement);
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
        
        //String statement = "";
        for (String line: lines) {
            statements.add(line);
        }
        
        return statements;
    }
}
