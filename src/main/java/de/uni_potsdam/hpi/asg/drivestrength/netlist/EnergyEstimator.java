package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.EstimatorCache;

/**
 * Consumed Energy of every Cell transitioning once for each input pin (with its chosen size and its total load)
 */
public class EnergyEstimator {
    protected static final Logger logger = LogManager.getLogger();

    private Netlist netlist;
    private DelayEstimator delayEstimator;
    private PowerEstimator powerEstimator;
    private boolean verbose;

    public EnergyEstimator(Netlist netlist, boolean verbose) {
        this.netlist = netlist;
        this.delayEstimator = new DelayEstimator(netlist, false, false);
        this.powerEstimator = new PowerEstimator(netlist, false);
        this.verbose = verbose;
    }

    public void print() {
        logger.info("Estimated Energy Sum: " + this.run());
    }

    public double run() {
        double sum = 0.0;
        for (CellInstance cellInstance : this.netlist.getRootModule().getCellInstances()) {
            EstimatorCache cache = cellInstance.getEstimatorCache();
            if (cache.isEnergyInvalidated()) {
                double sumForPin = 0.0;
                for (String pinName : cellInstance.getInputPinNames()) {
                    double loadCapacitance = cellInstance.getLoadCapacitanceSelected();
                    double delay = this.delayEstimator.estimateDelayFromRawDelayLines(cellInstance, pinName, loadCapacitance);
                    double transitionPower = this.powerEstimator.estimatePower(cellInstance, pinName);
                    double energy = delay * transitionPower;
                    sumForPin += energy;
                    if (verbose) {
                        logger.info("Energy for " + cellInstance.getDefinitionName() + " "
                                           + cellInstance.getName() + " " + pinName + ": " + energy);
                    }
                }
                cache.setEnergyValue(sumForPin);
            }
            sum += cache.getEnergyValue();
        }
        return sum;
    }
}
