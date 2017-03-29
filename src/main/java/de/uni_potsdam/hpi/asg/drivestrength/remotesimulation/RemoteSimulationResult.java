package de.uni_potsdam.hpi.asg.drivestrength.remotesimulation;

import java.util.HashMap;
import java.util.Map;

public class RemoteSimulationResult {
    private Map<String, Integer> testbenchSuccessTimes; //librarySuffix -> value
    private Map<String, Integer> sdfDelaySums; //librarySuffix -> value
    private double testbenchEnergy;

    public RemoteSimulationResult() {
        testbenchSuccessTimes = new HashMap<>();
        sdfDelaySums = new HashMap<>();
    }

    public void addTestbenchSuccessTime(String librarySuffix, int successTime) {
        testbenchSuccessTimes.put(librarySuffix, successTime);
    }

    public void addSdfDelaySum(String librarySuffix, int sdfDelaySum) {
        sdfDelaySums.put(librarySuffix, sdfDelaySum);
    }

    public void setTestbenchEnergy(double testbenchEnergyValue) {
        this.testbenchEnergy = testbenchEnergyValue;
    }

    public Map<String, Integer> getTestbenchSuccessTimes() {
        return testbenchSuccessTimes;
    }

    public Map<String, Integer> getSdfDelaySums() {
        return sdfDelaySums;
    }

    public int getSdfDelaySum(String librarySuffix) {
        return this.sdfDelaySums.get(librarySuffix);
    }

    public int getTestbenchSuccessTime(String librarySuffix) {
        return this.testbenchSuccessTimes.get(librarySuffix);
    }

    public double getTestbenchEnergy() {
        return testbenchEnergy;
    }

    public String toString() {
        return "RemoteSimulationResult: SDF delay sums: " + this.getSdfDelaySums() + ", testbench success times: " + this.getTestbenchSuccessTimes();
    }

}
