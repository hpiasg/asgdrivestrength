package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import java.util.List;
import java.util.Random;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class SimulatedAnnealingOptimizer {

    private Netlist netlist;
    private int roundCount;
    private double temperature;
    private DelayEstimator delayEstimator;
    private Random randomGenerator;

    public SimulatedAnnealingOptimizer(Netlist netlist, int rounds, boolean clampToImplementableCapacitances) {
        this.netlist = netlist;
        this.roundCount = rounds;
        this.delayEstimator = new DelayEstimator(netlist, false, false);
        this.randomGenerator = new Random();
    }
    
    public void run() {
        temperature = 5000;
        for (int i = 0; i < roundCount; i++) {
            double currentEnergy = delayEstimator.run();
            this.performRandomStep();
            double newEnergy = delayEstimator.run();
            if (newEnergy > currentEnergy) {
                double delta = newEnergy - currentEnergy;
                double condition = Math.exp(delta / this.temperature);
                if (Math.random() > condition) {
                    this.undoRandomStep();
                }
            }
            this.temperature *= 0.95;
        }
    }
    
    private void performRandomStep() {
        List<CellInstance> instances = this.netlist.getRootModule().getCellInstances();
        int index = randomGenerator.nextInt(instances.size());
        if (randomGenerator.nextFloat() > 0.5) {
            //TODO: instances.get(index).scaleUpOneStep();
        } else {
            //TODO: instances.get(index).scaleDownOneStep();
        }
    }
    
    private void undoRandomStep() {
        //TODO
    }
}
