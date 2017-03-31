package de.uni_potsdam.hpi.asg.drivestrength.optimization;

import java.util.Random;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class SimulatedAnnealingOptimizer extends AbstractDriveOptimizer {

    private int iterationCount;
    private boolean jumpInMutation;
    private double initialTemperature;
    private double alpha;
    private double temperature;
    private Random randomGenerator;
    private int indexForUndo;
    private Cell previousSizeForUndo;
    private SACostFunction costFunction;

    public SimulatedAnnealingOptimizer(Netlist netlist, boolean jumpNotStep, int roundsPerCell, int percentageEnergy) {
        super(netlist);
        this.jumpInMutation = jumpNotStep;
        this.costFunction = new SACostFunction(netlist, percentageEnergy);
        this.randomGenerator = new Random();
        this.selectParameters(roundsPerCell);
    }

    private void selectParameters(int roundsPerCell) {
        int cellCount = this.cellInstances.size();
        double expectedAvgDelta = this.costFunction.estimateAvgDelta();
        this.iterationCount = roundsPerCell * cellCount;
        int becomeGreedyAfter = (int) Math.round(iterationCount * 0.7);
        double initialAcceptanceP = 0.95;
        double greedyAcceptanceP = 0.05;
        this.initialTemperature = (-expectedAvgDelta) / Math.log(initialAcceptanceP);
        this.alpha = Math.pow(-expectedAvgDelta / (initialTemperature * Math.log(greedyAcceptanceP)), 1.0 / becomeGreedyAfter);

        logger.info("SA: i: " + this.iterationCount + ", alpha: " + alpha + ", T0: " + initialTemperature + ", G: " + becomeGreedyAfter);
    }

    @Override
    protected void optimize() {
        double beforeCost = this.costFunction.calculateCost();

        temperature = initialTemperature;
        for (int i = 0; i < iterationCount; i++) {
            double currentCost = this.costFunction.calculateCost();
            this.performRandomStep();
            double newCost = this.costFunction.calculateCost();
            if (newCost > currentCost) {
                double delta = newCost - currentCost;
                double condition = Math.exp(- delta / this.temperature);
                if (Math.random() > condition) {
                    this.undoRandomStep();
                }
            }
            this.temperature *= alpha;
        }
        logger.info("SA: result cost: " + this.costFunction.calculateCost() + " vs before " + beforeCost);
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
}
