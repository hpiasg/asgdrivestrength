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

    public SACostFunction(Netlist netlist, int percentageEnergy) {
        this.delayEstimator = new DelayEstimator(netlist, false, false);
        this.energyEstimator = new EnergyEstimator(netlist, false);

        this.weightEnergy = percentageEnergy / 100.0;
        this.weightDelay = 1 - weightEnergy;

        this.weightEnergy /= energyEstimator.run();
        this.weightDelay /= delayEstimator.run();
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

    public double estimateAvgDelta() {
        double expectedAvgDeltaEnergy = 0.002; //energy
        double expectedAvgDeltaDelay = 30; //delay, picoseconds
        double expectedAvgDelayWeighted = expectedAvgDeltaEnergy * weightEnergy + expectedAvgDeltaDelay * weightDelay;
        logger.info("SA expected avg score delta: " + expectedAvgDelayWeighted);
        return expectedAvgDelayWeighted;
    }
}
