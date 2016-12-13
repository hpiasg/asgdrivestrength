package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IndentBlockSeparator {
    private static final Pattern indentPattern = Pattern.compile("^(\\s*)(.*)");

    private List<String>statements;
    private Pattern startPattern;
    
    public IndentBlockSeparator(List<String> statements, Pattern startPattern) {
        this.statements = statements;
        this.startPattern = startPattern;
    }
    
    public List<List<String>> run() {
        List<List<String>> blocks = new ArrayList<>();
        List<String> currentBlockStatements = new ArrayList<>();
        

        boolean isReadingMatchingBlock = false;
        int blockIndent = -1;
        for (String statement : statements) {
            Matcher m = indentPattern.matcher(statement);
            if (isReadingMatchingBlock && m.matches()) {
                if (m.group(1).length() == blockIndent) {
                    blocks.add(currentBlockStatements); //add old block if a new one starts
                    isReadingMatchingBlock = false;
                } else {
                    currentBlockStatements.add(statement);
                }
            }
            m = startPattern.matcher(statement);
            if (m.matches()) {
                isReadingMatchingBlock = true;
                blockIndent = m.group(1).length();
                currentBlockStatements = new ArrayList<String>();
                currentBlockStatements.add(statement);
            }
        }
        if (currentBlockStatements.size() > 0) { //add last block
            blocks.add(currentBlockStatements);
        }
        return blocks;
    }
}
