package de.uni_potsdam.hpi.asg.drivestrength.cells;

import java.util.ArrayList;
import java.util.List;

public class DelayMatrix {
    private List<Double> loadCapacitanceSamples;
    private List<Double> inputSlewSamples;
    private List<Double> delayValues;
    
    public DelayMatrix() {
        this.loadCapacitanceSamples = new ArrayList<Double>();
        this.inputSlewSamples = new ArrayList<Double>();
        this.delayValues = new ArrayList<Double>();
    }
    
    public double getLoadCapacitanceAt(int loadCapacitanceIndex) {
        return this.loadCapacitanceSamples.get(loadCapacitanceIndex);
    }
    
    public double getInputSlewAt(int inputSlewIndex) {
        return this.inputSlewSamples.get(inputSlewIndex);
    }
    
    public double getDelayAt(int inputSlewIndex, int loadCapacitanceIndex) {
        return this.delayValues.get(7 * inputSlewIndex + loadCapacitanceIndex);
    }
    
    public void addInputSlewSample(double value) {
        this.inputSlewSamples.add(value);
    }
    
    public void addLoadCapacitanceSample(double value) {
        this.loadCapacitanceSamples.add(value);
    }
    
    public void addDelayValue(double value) {
        this.delayValues.add(value);
    }
}
