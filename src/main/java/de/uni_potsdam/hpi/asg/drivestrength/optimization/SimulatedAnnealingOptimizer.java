package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import java.util.Random;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.DelayEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.EnergyEstimator;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class SimulatedAnnealingOptimizer extends AbstractDriveOptimizer {

    private int roundsPerCell;
    private int iterationCount;
    private boolean jumpInMutation;
    private double initialTemperature;
    private double alpha;
    private double temperature;
    private DelayEstimator delayEstimator;
    private EnergyEstimator energyEstimator;
    private Random randomGenerator;
    private int indexForUndo;
    private Cell previousSizeForUndo;
    private int percentageEnergy;

    public SimulatedAnnealingOptimizer(Netlist netlist, boolean jumpNotStep, int roundPerCell, int percentageEnergy) {
        super(netlist);
        this.roundsPerCell = roundPerCell;
        this.jumpInMutation = jumpNotStep;
        this.delayEstimator = new DelayEstimator(netlist, false, false);
        this.energyEstimator = new EnergyEstimator(netlist, false);
        this.randomGenerator = new Random();
        this.selectParameters();
        this.percentageEnergy = percentageEnergy;
    }

    private void selectParameters() {
        int cellCount = this.cellInstances.size();
        double expectedAvgDelta = 0.002; //energy
        //double expectedAvgDelta = 30; //delay
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
        double beforeCost = calculateCost();

        temperature = initialTemperature;
        for (int i = 0; i < iterationCount; i++) {
            double currentCost = calculateCost();
            this.performRandomStep();
            double newCost = calculateCost();
            if (newCost > currentCost) {
                double delta = newCost - currentCost;
                double condition = Math.exp(- delta / this.temperature);
                if (Math.random() > condition) {
                    this.undoRandomStep();
                }
            }
            this.temperature *= alpha;
        }
        logger.info("SA: result cost: " + calculateCost() + " vs before " + beforeCost);
    }

    private void performRandomStep() {
        int index = randomGenerator.nextInt(this.cellInstances.size());
        CellInstance instance = this.cellInstances.get(index);
        indexForUndo = index;
        previousSizeForUndo = instance.getSelectedSize();
        if (jumpInMutation) {
            instance.selectRandomSize();
        } else {
            if (randomGenerator.nextFloat() > 0.5) {
                instance.selectNextBiggerSizeIfPossible();
            } else {
                instance.selectNextSmallerSizeIfPossible();
            }
        }
    }

    private void undoRandomStep() {
        this.cellInstances.get(indexForUndo).selectSize(previousSizeForUndo);
    }

    private double calculateCost() {
        double weightEnergy = this.percentageEnergy / 100.0;
        double weightDelay = (100 - this.percentageEnergy) / 100.0;
        return energyEstimator.run() * weightEnergy + delayEstimator.run() * weightDelay;
    }
}
