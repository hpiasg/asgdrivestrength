package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import java.util.List;
import java.util.Random;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class SimulatedAnnealingOptimizer {

    private Netlist netlist;
    private int roundsPerCell;
    private int iterationCount;
    private double initialTemperature;
    private double alpha;
    private double temperature;
    private DelayEstimator delayEstimator;
    private Random randomGenerator;
    private int undoIndex;
    private Cell undoPreviousSize;

    public SimulatedAnnealingOptimizer(Netlist netlist, int roundPerCell) {
        this.netlist = netlist;
        this.roundsPerCell = roundPerCell;
        this.delayEstimator = new DelayEstimator(netlist, false, false);
        this.randomGenerator = new Random();
        this.selectParameters();
    }

    private void selectParameters() {
        int cellCount = netlist.getRootModule().getCellInstances().size();
        int expectedAvgDelta = 50; //from test with count10 and selectRandomSize
        this.iterationCount = roundsPerCell * cellCount;
        int becomeGreedyAfter = (int) Math.round(iterationCount * 0.7);
        double initialAcceptanceP = 0.95;
        double greedyAcceptanceP = 0.05;
        this.initialTemperature = (-expectedAvgDelta) / Math.log(initialAcceptanceP);
        this.alpha = Math.pow(-expectedAvgDelta / (initialTemperature * Math.log(greedyAcceptanceP)), 1.0 / becomeGreedyAfter);


        System.out.println("alpha: " + alpha + ", T0: " + initialTemperature + ", G: " + becomeGreedyAfter);
    }

    public void run() {
        double beforeEnergy = delayEstimator.run();
        long startTime = System.currentTimeMillis();

        temperature = initialTemperature;
        for (int i = 0; i < iterationCount; i++) {
            double currentEnergy = delayEstimator.run();
            this.performRandomStep();
            double newEnergy = delayEstimator.run();
            if (newEnergy > currentEnergy) {
                double delta = newEnergy - currentEnergy;
                double condition = Math.exp(- delta / this.temperature);
                //System.out.println("rejecting if random > " + condition);
                if (Math.random() > condition) {
                    this.undoRandomStep();
                }
            }
            //System.out.print(Math.round(currentEnergy));
            this.temperature *= alpha;
        }
        long stopTime = System.currentTimeMillis();
        System.out.println("SA took " + (stopTime - startTime) + " ms, result energy: " + delayEstimator.run() + " vs before " + beforeEnergy);

    }

    private void performRandomStep() {
        List<CellInstance> instances = this.netlist.getRootModule().getCellInstances();
        int index = randomGenerator.nextInt(instances.size());
        CellInstance instance = instances.get(index);
        undoIndex = index;
        undoPreviousSize = instance.getSelectedSize();
        //instance.selectRandomSize();
        if (randomGenerator.nextFloat() > 0.5) {
            instance.selectNextBiggerSizeIfPossible();
        } else {
            instance.selectNextSmallerSizeIfPossible();
        }
    }

    private void undoRandomStep() {
        List<CellInstance> instances = this.netlist.getRootModule().getCellInstances();
        instances.get(undoIndex).selectSize(undoPreviousSize);
    }
}
