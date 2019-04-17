package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.EnergyEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.PowerEstimator;

public class SACostFunction {
    protected static final Logger logger = LogManager.getLogger();

    private DelayEstimator delayEstimator;
    private EnergyEstimator energyEstimator;
    private PowerEstimator powerEstimator;
    
    private double weightDelay;
    private double weightEnergy;
    private double weightPower;
    
    private double avgDeltaDelay;
    private double avgDeltaEnergy;
    private double avgDeltaPower;
    
    public SACostFunction(Netlist netlist, int factorDelay, int factorEnergy, int factorPower) {
//        logger.info("SA: using energy weight " + percentageEnergy + " %");
        this.delayEstimator = new DelayEstimator(netlist, false, false);
        this.energyEstimator = new EnergyEstimator(netlist, false);
        this.powerEstimator = new PowerEstimator(netlist, false);
        
        int factorSum = factorDelay + factorPower + factorEnergy; 
        this.weightDelay = (float) factorDelay / factorSum;
        this.weightEnergy = (float) factorEnergy / factorSum;
        this.weightPower = (float) factorPower / factorSum;
        
        logger.info("SA: using delay weight:" + weightDelay + ", energy weight:" + weightEnergy + ", power weight:" + weightPower);
        
        this.weightDelay /= delayEstimator.run();
        this.weightEnergy /= energyEstimator.run();
        this.weightPower /= powerEstimator.run();
        
//        logger.info("SA: using delay weight:" + weightDelay + ", energy weight:" + weightEnergy + ", power weight:" + weightPower);
    }
    

    public void setCalibrationDeltas(double avgDeltaDelay, double avgDeltaEnergy, double avgDeltaPower) {
        this.avgDeltaDelay = avgDeltaDelay;
        this.avgDeltaEnergy = avgDeltaEnergy;
        this.avgDeltaPower = avgDeltaPower;
    }

    public double calculateCost() {
//        if (weightEnergy < 0.00000001) {
//            return delayEstimator.run() * weightDelay;
//        }
//        if (weightDelay < 0.00000001) {
//            return energyEstimator.run() * weightEnergy;
//        }
        return delayEstimator.run() * weightDelay + energyEstimator.run() * weightEnergy + powerEstimator.run() * weightPower;
    }

    public double estimateEnergy() {
        return energyEstimator.run();
    }

    public double estimateDelay() {
        return delayEstimator.run();
    }
    
    public double estimatePower() {
        return powerEstimator.run();
    }

    public double estimateAvgDeltaWeighted() {
//        double expectedAvgDeltaEnergy = 0.002;
//        double expectedAvgDeltaDelay = 30;
        logger.info("SA: estimated avg deltas: delay: " + this.avgDeltaDelay + ", energy: " + this.avgDeltaEnergy + ", power: " + this.avgDeltaPower);
        double expectedAvgDelayWeighted = this.avgDeltaDelay * weightDelay + this.avgDeltaEnergy * weightEnergy + this.avgDeltaPower * weightPower;
        logger.info("SA: estimated avg score delta: " + expectedAvgDelayWeighted);
        return expectedAvgDelayWeighted;
    }
}
