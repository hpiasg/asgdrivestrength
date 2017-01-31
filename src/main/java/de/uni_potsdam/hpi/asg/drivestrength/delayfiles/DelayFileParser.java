package de.uni_potsdam.hpi.asg.drivestrength.delayfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;

public class DelayFileParser {
    
    private static final Pattern indentPattern = Pattern.compile("^(\\s*)(.*)");
    private static final Pattern cellTypePattern = Pattern.compile("CELLTYPE \"(.*)\"");
    private static final Pattern instanceNamePattern = Pattern.compile("INSTANCE (.*)");
    private static final Pattern delayPattern = Pattern.compile("(.*)IOPATH (.*) (.*) \\((.*)\\) \\((.*)\\)(.*)");
    
    private List<String> lines;
    
    String currentCellType;
    String currentInstanceName;
    
    
    public DelayFileParser(File delayFile) {
        this.readLinesFromFile(delayFile);
    }
    
    private void readLinesFromFile(File delayFile) {
        this.lines = new ArrayList<>();
        List<String> linesRaw = FileHelper.getInstance().readFile(delayFile);
        assert(linesRaw != null);
        for (String line : linesRaw) {
            line = line.trim();
            if (line.charAt(0) == '(') {
                line = line.substring(1, line.length());
            }
            if (line.charAt(line.length() - 1) == ')') {
                line = line.substring(0, line.length() - 1);
            }
            if (line.length() > 0) {
                lines.add(line);                    
            }
        }
    }
    
    public void run() {
        for (String line : lines) {
            if (parseCellTypeStatement(line)) continue;
            if (parseInstanceNameStatement(line)) continue;
            if (parseDelayStatement(line)) continue;
        }
    }
    
    private boolean parseCellTypeStatement(String line) {
        Matcher m = cellTypePattern.matcher(line);
        if (m.matches()) {
            this.currentCellType = m.group(1);
            System.out.println();
            System.out.print(currentCellType + " ");
            return true;
        }
        return false;
    }

    private boolean parseInstanceNameStatement(String line) {
        Matcher m = instanceNamePattern.matcher(line);
        if (m.matches()) {
            this.currentInstanceName = m.group(1);
            System.out.println(currentInstanceName);
            return true;
        }
        return false;
    }
    
    private boolean parseDelayStatement(String line) {
        Matcher m = delayPattern.matcher(line);
        if (m.matches()) {
            String pinName = m.group(2);
            String riseDelays = m.group(4);
            String fallDelays = m.group(5).replace(")", "");
            double riseDelay = parseDelayTriple(riseDelays);
            double fallDelay = parseDelayTriple(fallDelays);
            double avgDelay = (riseDelay + fallDelay) / 2 * 1000;
            System.out.println(pinName + ": " + avgDelay + " ps");
            return true;
        }
        return false;
    }
    
    private double parseDelayTriple(String triple) {
        String oneValue = triple.split(":")[0];
        return Double.parseDouble(oneValue);
    }
    
}
