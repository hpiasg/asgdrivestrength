package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import java.util.Random;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class SimulatedAnnealingOptimizer extends AbstractDriveOptimizer {

    private int roundsPerCell;
    private int iterationCount;
    private double initialTemperature;
    private double alpha;
    private double temperature;
    private DelayEstimator delayEstimator;
    private Random randomGenerator;
    private int indexForUndo;
    private Cell previousSizeForUndo;

    public SimulatedAnnealingOptimizer(Netlist netlist, int roundPerCell) {
        super(netlist);
        this.roundsPerCell = roundPerCell;
        this.delayEstimator = new DelayEstimator(netlist, false, false);
        this.randomGenerator = new Random();
        this.selectParameters();
    }

    private void selectParameters() {
        int cellCount = this.cellInstances.size();
        int expectedAvgDelta = 50; //from test with count10 and selectRandomSize
        this.iterationCount = roundsPerCell * cellCount;
        int becomeGreedyAfter = (int) Math.round(iterationCount * 0.7);
        double initialAcceptanceP = 0.95;
        double greedyAcceptanceP = 0.05;
        this.initialTemperature = (-expectedAvgDelta) / Math.log(initialAcceptanceP);
        this.alpha = Math.pow(-expectedAvgDelta / (initialTemperature * Math.log(greedyAcceptanceP)), 1.0 / becomeGreedyAfter);

        logger.info("SA: alpha: " + alpha + ", T0: " + initialTemperature + ", G: " + becomeGreedyAfter);
    }

    @Override
    protected void optimize() {
        double beforeEnergy = calculateEnergy();

        temperature = initialTemperature;
        for (int i = 0; i < iterationCount; i++) {
            double currentEnergy = calculateEnergy();
            this.performRandomStep();
            double newEnergy = calculateEnergy();
            if (newEnergy > currentEnergy) {
                double delta = newEnergy - currentEnergy;
                double condition = Math.exp(- delta / this.temperature);
                if (Math.random() > condition) {
                    this.undoRandomStep();
                }
            }
            this.temperature *= alpha;
        }
        logger.info("SA: result energy: " + calculateEnergy() + " vs before " + beforeEnergy);
    }

    private void performRandomStep() {
        int index = randomGenerator.nextInt(this.cellInstances.size());
        CellInstance instance = this.cellInstances.get(index);
        indexForUndo = index;
        previousSizeForUndo = instance.getSelectedSize();
        //instance.selectRandomSize();
        if (randomGenerator.nextFloat() > 0.5) {
            instance.selectNextBiggerSizeIfPossible();
        } else {
            instance.selectNextSmallerSizeIfPossible();
        }
    }

    private void undoRandomStep() {
        this.cellInstances.get(indexForUndo).selectSize(previousSizeForUndo);
    }

    private double calculateEnergy() {
        return delayEstimator.run();
    }
}
