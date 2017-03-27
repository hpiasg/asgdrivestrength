package de.uni_potsdam.hpi.asg.drivestrength.cells;

import java.util.ArrayList;
import java.util.List;

public class PowerMatrix7x7 {

        private List<Double> loadCapacitanceSamples;
        private List<Double> inputSlewSamples;
        private List<Double> powerValues;

        public PowerMatrix7x7() {
            this.loadCapacitanceSamples = new ArrayList<Double>();
            this.inputSlewSamples = new ArrayList<Double>();
            this.powerValues = new ArrayList<Double>();
        }

        public double getLoadCapacitanceAt(int loadCapacitanceIndex) {
            return this.loadCapacitanceSamples.get(loadCapacitanceIndex);
        }

        public double getInputSlewAt(int inputSlewIndex) {
            return this.inputSlewSamples.get(inputSlewIndex);
        }

        public double getPowerAt(int inputSlewIndex, int loadCapacitanceIndex) {
            return this.powerValues.get(7 * inputSlewIndex + loadCapacitanceIndex);
        }

        public void addInputSlewSample(double value) {
            this.inputSlewSamples.add(value);
        }

        public void addLoadCapacitanceSample(double value) {
            this.loadCapacitanceSamples.add(value);
        }

        public void addPowerValue(double value) {
            this.powerValues.add(value);
        }
}
