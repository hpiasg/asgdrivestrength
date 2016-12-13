package de.uni_potsdam.hpi.asg.drivestrength.cells;

import java.util.ArrayList;
import java.util.List;

public class DelayMatrix {
    private List<Double> outputCapacitanceSamples;
    private List<Double> inputSlewSamples;
    private List<Double> delayValues;
    
    public DelayMatrix() {
        this.outputCapacitanceSamples = new ArrayList<Double>();
        this.inputSlewSamples = new ArrayList<Double>();
        this.delayValues = new ArrayList<Double>();
    }
    
    public double getOutputCapacitanceAt(int outputCapacitanceIndex) {
        return this.outputCapacitanceSamples.get(outputCapacitanceIndex);
    }
    
    public double getInputSlewAt(int inputSlewIndex) {
        return this.inputSlewSamples.get(inputSlewIndex);
    }
    
    public double getDelayAt(int inputSlewIndex, int outputCapacitanceIndex) {
        return this.delayValues.get(7 * inputSlewIndex + outputCapacitanceIndex);
    }
    
    public void addInputSlewSample(double value) {
        this.inputSlewSamples.add(value);
    }
    
    public void addOutputCapacitanceSample(double value) {
        this.outputCapacitanceSamples.add(value);
    }
    
    public void addDelayValue(double value) {
        this.delayValues.add(value);
    }
}
