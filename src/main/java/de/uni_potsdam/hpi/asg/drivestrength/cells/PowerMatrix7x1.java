package de.uni_potsdam.hpi.asg.drivestrength.cells;

import java.util.ArrayList;
import java.util.List;

public class PowerMatrix7x1 {

    private List<Double> inputSlewSamples;
    private List<Double> powerValues;

    public PowerMatrix7x1() {
        this.inputSlewSamples = new ArrayList<Double>();
        this.powerValues = new ArrayList<Double>();
    }

    public double getInputSlewAt(int inputSlewIndex) {
        return this.inputSlewSamples.get(inputSlewIndex);
    }

    public double getPowerAt(int inputSlewIndex) {
        return this.powerValues.get(inputSlewIndex);
    }

    public void addInputSlewSample(double value) {
        this.inputSlewSamples.add(value);
    }

    public void addPowerValue(double value) {
        this.powerValues.add(value);
    }
}
