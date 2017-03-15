package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;

public class AggregatedCell {

    private String name;
    private Map<String, Map<String, Double>> sizeCapacitances; //pin->size->value
    private Map<String, Map<String, Double>> monotonizedSizeCapacitances; // pin->size->value
    private Map<String, DelayParameterTriple> delayParameterTriples; //pin->triple
    private Map<String, Map<String, DelayLine>> sizeDelayLines; //pin->size->value
    private List<String> orderedPinNames;
    private List<String> inputPinNames;
    private String outputPinName;
    private List<String> sizeNames;
    private List<Cell> sizesRaw;
    private String defaultSizeName;
    private Random randomGenerator;

    protected static final Logger logger = LogManager.getLogger();

    public String getName() {
        return name;
    }

    public AggregatedCell(String name) {
        this.name = name;
        this.delayParameterTriples = new HashMap<>();
        this.sizeNames = new ArrayList<>();
        this.sizesRaw = new ArrayList<>();
        this.randomGenerator = new Random();
    }

    public void setDefaultSizeName(String defaultSizeName) {
        this.defaultSizeName = defaultSizeName;
    }

    public void setInputPinNames(List<String> inputPinNames) {
    	this.inputPinNames = inputPinNames;
    }

    public List<String> getInputPinNames() {
        return this.inputPinNames;
    }

    public String getOutputPinName() {
        return outputPinName;
    }

    public void setOutputPinName(String outputPinName) {
        this.outputPinName = outputPinName;
    }

    public List<String> getOrderedPinNames() {
        return orderedPinNames;
    }

    public void setOrderedPinNames(List<String> orderedPinNames) {
        this.orderedPinNames = orderedPinNames;
    }

    public void addCellSize(Cell cellSizeRaw) {
    	this.sizesRaw.add(cellSizeRaw);
        this.sizeNames.add(cellSizeRaw.getName());
    }

    public void orderRawSizes(List<String> orderedSizeNames) {
        for (Cell rawCell : this.sizesRaw) {
            if (!orderedSizeNames.contains(rawCell.getName())) {
                logger.warn("Size " + rawCell.getName() + " missing in ordered size names. Skipping it in AggregatedCell.");
            }
        }

        List<Cell> orderedSizesRaw = new ArrayList<>();
        for (String name : orderedSizeNames) {
            Cell rawCell = this.getSizeByName(name);
            orderedSizesRaw.add(rawCell);
        }
        this.sizesRaw = orderedSizesRaw;
    }

    public void setMonotonizedSizeCapacitances(Map<String, Map<String, Double>> monotonizedSizeCapacitances) {
        this.monotonizedSizeCapacitances = monotonizedSizeCapacitances;
    }

    public List<Cell> getRawSizes() {
    	return this.sizesRaw;
    }

    public Cell getRandomSize() {
        return this.sizesRaw.get(randomGenerator.nextInt(this.sizesRaw.size()));
    }

    public Cell getNextBiggerSizeTo(Cell previousSize) {
        //assumes that sizesRaw has previously been ordered
        for (int i = 0; i < this.sizesRaw.size() - 1; i++) {
            if (this.sizesRaw.get(i) == previousSize) {
                return this.sizesRaw.get(i + 1);
            }
        }
        return previousSize;
    }

    public Cell getNextSmallerSizeTo(Cell previousSize) {
        //assumes that sizesRaw has previously been ordered
        for (int i = 1; i < this.sizesRaw.size(); i++) {
            if (this.sizesRaw.get(i) == previousSize) {
                return this.sizesRaw.get(i - 1);
            }
        }
        return previousSize;
    }

    public boolean containsSizeName(String cellName) {
        return this.sizeNames.contains(cellName);
    }

    public Map<String, Map<String, Double>> getSizeCapacitances() {
		return sizeCapacitances;
	}

	public void setSizeCapacitances(Map<String, Map<String, Double>> sizeCapacitances) {
		this.sizeCapacitances = sizeCapacitances;
	}

	public double getSizeCapacitance(String sizeName, String pinName) {
	    return this.sizeCapacitances.get(pinName).get(sizeName);
	}

	public void setDelayParameterTriples(Map<String, DelayParameterTriple> delayParameterTriples) {
	    this.delayParameterTriples = delayParameterTriples;
	}

	public Map<String, DelayParameterTriple> getDelayParameterTriples() {
	    return this.delayParameterTriples;
	}

	public void setSizeDelayLines(Map<String, Map<String, DelayLine>> sizeDelayLines) {
        this.sizeDelayLines = sizeDelayLines;
    }

    public Map<String, Map<String, DelayLine>> getSizeDelayLines() {
        return sizeDelayLines;
    }

    public double getParasiticDelayForPin(String pinName) {
        return this.delayParameterTriples.get(pinName).getParasiticDelay();
    }

    public double getLogicalEffortForPin(String pinName) {
        return this.delayParameterTriples.get(pinName).getLogicalEffort();
    }

    public double getAvgLogicalEffort() {
        double sum = 0;
        for (DelayParameterTriple t: this.delayParameterTriples.values()) {
            sum += t.getLogicalEffort();
        }
        return sum / this.delayParameterTriples.size();
    }

    public double getAvgParasiticDelay() {
        double sum = 0;
        for (DelayParameterTriple t : this.delayParameterTriples.values()) {
            sum += t.getParasiticDelay();
        }
        return sum / this.delayParameterTriples.size();
    }

    public int getStageCountForPin(String pinName) {
    	return this.delayParameterTriples.get(pinName).getStageCount();
    }

    public Cell getSizeForInputCapacitances(Map<String, Double> desiredInputPinCapacitances) {
        double lowestDeviation = Double.POSITIVE_INFINITY;
        Cell bestSize = this.getDefaultSize();

        for (Cell size : this.sizesRaw) {
            double deviation = computeDeviationToDesiredCapacitances(size.getName(), desiredInputPinCapacitances);
            if (deviation < lowestDeviation) {
                lowestDeviation = deviation;
                bestSize = size;
            }
        }
        return bestSize;
    }

    public Cell getFastestSizeForLoad(double loadCapacitance) {
        Cell fastestSize = this.getDefaultSize();
        double lowestAvgDelay = Double.POSITIVE_INFINITY;
        for (Cell c : this.sizesRaw) {
            double delaySum = 0.0;
            int delayCount = 0;
            for (String pinName : this.inputPinNames) {
                double inputCapacitance = this.sizeCapacitances.get(pinName).get(c.getName());
                double electricalEffort = loadCapacitance / inputCapacitance;
                double delay = this.sizeDelayLines.get(pinName).get(c.getName()).valueAtX(electricalEffort);
                delaySum += delay;
                delayCount++;
            }
            double avgDelay = delaySum / delayCount;
            if (avgDelay < lowestAvgDelay) {
                lowestAvgDelay = avgDelay;
                fastestSize = c;
            }
        }
        return fastestSize;
    }

    private double computeDeviationToDesiredCapacitances(String sizeName, Map<String, Double> desiredCapacitances) {
        double deviation = 0.0;
        for (String pinName : desiredCapacitances.keySet()) {
            double actual = this.getMonotonizedOrRegularSizeCapacitance(pinName, sizeName);
            double desired = desiredCapacitances.get(pinName);
            deviation += Math.abs(actual - desired);
        }
        return deviation;
    }

    private double getMonotonizedOrRegularSizeCapacitance(String pinName, String sizeName) {
        if (this.monotonizedSizeCapacitances == null) {
            return this.sizeCapacitances.get(pinName).get(sizeName);
        }
        return this.monotonizedSizeCapacitances.get(pinName).get(sizeName);
    }

    public double getLargestPossibleCapacitance(String pinName) {
        double largestC = 0.0;
        for (Cell rawCell : this.sizesRaw) {
            if (rawCell.getCapacitanceForPin(pinName) > largestC) {
                largestC = rawCell.getCapacitanceForPin(pinName);
            }
        }
        return largestC;
    }

    public double getSmallestPossibleCapacitance(String pinName) {
        double smallestC = Double.MAX_VALUE;
        for (Cell rawCell : this.sizesRaw) {
            if (rawCell.getCapacitanceForPin(pinName) < smallestC) {
                smallestC = rawCell.getCapacitanceForPin(pinName);
            }
        }
        return smallestC;
    }

    public int getSizeCount() {
        return this.sizeNames.size();
    }

    public String getPinNameAtPosition(int position) {
        return this.orderedPinNames.get(position);
    }

    public double getDefaultCapacitanceForPin(String pinName) {
        Cell rawCell = this.getDefaultSize();
        return rawCell.getCapacitanceForPin(pinName);
    }

    public Cell getDefaultSize() {
        for (Cell rawCell : this.sizesRaw) {
            if (rawCell.getName().equals(this.defaultSizeName)) {
                return rawCell;
            }
        }
        return this.sizesRaw.get(0);
    }

    public String toString() {
        return "AggregatedCell " + this.getName() + " with " + this.getSizeCount() + " cell sizes";
    }

    public boolean isSingleStageCell() {
        for (String pinName : this.getInputPinNames()) {
            int stageCount = this.getStageCountForPin(pinName);
            if (stageCount > 1) {
                return false;
            }
        }
        return true;
    }

    private Cell getSizeByName(String name) {
        for (Cell c : this.sizesRaw) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        throw new Error("AggregatedCell " + this.getName() + " has no raw size of name " + name);
    }
}
