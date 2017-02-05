package de.uni_potsdam.hpi.asg.drivestrength.delayfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;

public class DelayFileParser {
    
    private static final Pattern cellTypePattern = Pattern.compile("CELLTYPE \"(.*)\"");
    private static final Pattern instanceNamePattern = Pattern.compile("INSTANCE (.*)");
    private static final Pattern delayPattern = Pattern.compile("(.*)IOPATH (.*) (.*) \\((.*)\\) \\((.*)\\)(.*)");
    
    private List<String> lines;
    
    private String currentCellType;
    private String currentInstanceName;
    
    private Map<String, Map<String, List<Double>>> delays;
    private Map<String, Map<String, Double>> avgDelays;
    private Map<String, String> cellTypes;
    
    
    public DelayFileParser(File delayFile) {
        this.readLinesFromFile(delayFile);
    }
    
    private void readLinesFromFile(File delayFile) {
        this.lines = new ArrayList<>();
        List<String> linesRaw = FileHelper.getInstance().readFile(delayFile);
        assert(linesRaw != null);
        for (String line : linesRaw) {
            line = line.trim();
            line = line.replace("posedge ", "");
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
    
    public void parse() {
        delays = new HashMap<>();
        cellTypes = new HashMap<>();
        for (String line : lines) {
            if (parseCellTypeStatement(line)) continue;
            if (parseInstanceNameStatement(line)) continue;
            if (parseDelayStatement(line)) continue;
        }
        
        computeAvgDelays();
    }
    
    public void printAll() {        
        for (String instanceName : avgDelays.keySet()) {
            for (String pinName : avgDelays.get(instanceName).keySet()) {
                String cellType = Cell.sortableName(cellTypes.get(instanceName));
                System.out.println(cellType + "__" + pinName + "__" + instanceName + ", " + avgDelays.get(instanceName).get(pinName));
            }
        }
    }
    
    public int getDelaySum() {
        double sum = 0;
        for (String instanceName : avgDelays.keySet()) {
            for (String pinName : avgDelays.get(instanceName).keySet()) {
                sum += avgDelays.get(instanceName).get(pinName);
            }
        }
        return (int) Math.round(sum);
    }
    
    private boolean parseCellTypeStatement(String line) {
        Matcher m = cellTypePattern.matcher(line);
        if (m.matches()) {
            this.currentCellType = m.group(1);
            return true;
        }
        return false;
    }

    private boolean parseInstanceNameStatement(String line) {
        Matcher m = instanceNamePattern.matcher(line);
        if (m.matches()) {
            this.currentInstanceName = m.group(1);
            this.cellTypes.put(currentInstanceName, currentCellType);
            return true;
        }
        return false;
    }
    
    private boolean parseDelayStatement(String line) {
        Matcher m = delayPattern.matcher(line);
        if (m.matches()) {
            String pinName = m.group(2);
            pinName = pinName.replace(")", "").replaceAll("\\(", "");
            String riseDelays = m.group(4);
            String fallDelays = m.group(5).replace(")", "");
            double riseDelay = parseDelayTriple(riseDelays);
            double fallDelay = parseDelayTriple(fallDelays);
            double avgDelay = (riseDelay + fallDelay) / 2 * 1000;
            
            if (!delays.containsKey(currentInstanceName)) {
                delays.put(currentInstanceName, new HashMap<>());
            }
            if (!(delays.get(currentInstanceName)).containsKey(pinName)) {
                delays.get(currentInstanceName).put(pinName, new ArrayList<>());
            }
            delays.get(currentInstanceName).get(pinName).add(avgDelay);
            
            return true;
        }
        return false;
    }
    
    private double parseDelayTriple(String triple) {
        String oneValue = triple.split(":")[0];
        return Double.parseDouble(oneValue);
    }
    
    private void computeAvgDelays() {
        avgDelays = new HashMap<>();
        for (String instanceName : this.delays.keySet()) {
            avgDelays.put(instanceName, new HashMap<>());
            for (String pinName : this.delays.get(instanceName).keySet()) {
                double sum = 0.0;
                int count = 0;
                for (double delay : delays.get(instanceName).get(pinName)) {
                    sum += delay;
                    count++;
                }
                avgDelays.get(instanceName).put(pinName, sum / count);
            }
        }
    }
}
