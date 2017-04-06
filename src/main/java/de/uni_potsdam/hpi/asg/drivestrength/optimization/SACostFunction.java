package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.EnergyEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;

public class SACostFunction {
    protected static final Logger logger = LogManager.getLogger();

    private DelayEstimator delayEstimator;
    private EnergyEstimator energyEstimator;
    private double weightEnergy;
    private double weightDelay;
    private double avgDeltaEnergy;
    private double avgDeltaDelay;

    public SACostFunction(Netlist netlist, int percentageEnergy) {
        logger.info("SA: using energy weight " + percentageEnergy + " %");
        this.delayEstimator = new DelayEstimator(netlist, false, false);
        this.energyEstimator = new EnergyEstimator(netlist, false);

        this.weightEnergy = percentageEnergy / 100.0;
        this.weightDelay = 1 - weightEnergy;

        this.weightEnergy /= energyEstimator.run();
        this.weightDelay /= delayEstimator.run();
    }

    public void setCalibrationDeltas(double avgDeltaEnergy, double avgDeltaDelay) {
        this.avgDeltaEnergy = avgDeltaEnergy;
        this.avgDeltaDelay = avgDeltaDelay;
    }

    public double calculateCost() {
        if (weightEnergy < 0.00000001) {
            return delayEstimator.run() * weightDelay;
        }
        if (weightDelay < 0.00000001) {
            return energyEstimator.run() * weightEnergy;
        }
        return energyEstimator.run() * weightEnergy + delayEstimator.run() * weightDelay;
    }

    public double estimateEnergy() {
        return energyEstimator.run();
    }

    public double estimateDelay() {
        return delayEstimator.run();
    }

    public double estimateAvgDeltaWeighted() {
//        double expectedAvgDeltaEnergy = 0.002;
//        double expectedAvgDeltaDelay = 30;
        logger.info("SA: estimated avg deltas: energy: " + this.avgDeltaEnergy + ", delay: " + this.avgDeltaDelay);
        double expectedAvgDelayWeighted = this.avgDeltaEnergy * weightEnergy + this.avgDeltaDelay * weightDelay;
        logger.info("SA: estimated avg score delta: " + expectedAvgDelayWeighted);
        return expectedAvgDelayWeighted;
    }
}
