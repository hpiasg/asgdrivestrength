package de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCellLibrary;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;

public class VerilogParser {
    protected static final Logger logger = LogManager.getLogger();
    
    private static final Pattern endmodulePattern = Pattern.compile("^\\s*endmodule\\s*$");
    private static final Pattern statementPattern = Pattern.compile("^.*;$");
    
    private List<String> statements;
    private AggregatedCellLibrary aggregatedCellLibrary;

    public VerilogParser(File verilogFile, AggregatedCellLibrary aggregatedCellLibrary) {
        this.statements = readVerilogStatementsFromFile(verilogFile);
        this.aggregatedCellLibrary = aggregatedCellLibrary;
    }
    
    public VerilogParser(File verilogFile) {
        this.statements = readVerilogStatementsFromFile(verilogFile);
    }
    
    private List<String> readVerilogStatementsFromFile(File verilogFile) {
        List<String> lines = FileHelper.getInstance().readFile(verilogFile);
        assert(lines != null);
        List<String> statements = mergeMultilineStatements(lines);
        return statements;
    }
    
    private List<String> mergeMultilineStatements(List<String> lines) {
        List<String> statements = new ArrayList<String>();
        
        String statement = "";
        String comment = "";
        for (String line: lines) {
            line = line.trim();
            if(line.contains("//")) {
                comment += line.substring(line.indexOf("//")); 
                line = line.substring(0, line.indexOf("//"));
            }
            if(line.equals("")) {
                continue;
            }
            statement += line;
            if (matches(line, statementPattern) || matches(line, endmodulePattern) ) {
                if(!comment.equals("")) {
                    statements.add(comment);
                }
                statements.add(statement);
                statement = "";
                comment = "";
            } else {
                statement += " ";
            }
        }
        
        return statements;
    }
    
    private boolean matches(String aString, Pattern aPattern) {
        Matcher m = aPattern.matcher(aString);
        return m.matches();
    }
    

    
    public Netlist createNetlist() {
        logger.info("Parsing Verilog netlist...");
        
        Netlist netlist = new Netlist();
        
        List<String> currentModuleStatements = new ArrayList<String>();
        
        for (String statement: statements) {
            currentModuleStatements.add(statement);
            if (matches(statement, endmodulePattern)) {
                Module module = new VerilogModuleParser(currentModuleStatements, netlist, aggregatedCellLibrary).run();
                netlist.addModule(module);
                currentModuleStatements = new ArrayList<String>();
            }
        }
        
        logger.info("Parsing Verilog netlist complete");
        
        return netlist;
    }
}
