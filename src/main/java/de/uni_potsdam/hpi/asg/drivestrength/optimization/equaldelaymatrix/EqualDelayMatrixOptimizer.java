package de.uni_potsdam.hpi.asg.drivestrength.optimization.equaldelaymatrix;

import java.util.List;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.Load;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class EqualDelayMatrixOptimizer {

    private List<CellInstance> cellInstances;
    private RealMatrix effortMatrix_T;
    private RealMatrix staticLoadMatrix_b;
    private RealMatrix driveStrengthMatrix_x;
    private double criticalDelay;


    public EqualDelayMatrixOptimizer(Netlist netlist) {
        this.cellInstances = netlist.getRootModule().getCellInstances();
        for (CellInstance i: this.cellInstances) {
            if (!(i.getDefinition().isSingleStageCell())) {
                throw new Error("Equal Delay Matrix Optimizer works only on all-single-stage netlists");
            }
        }
    }

    public void run() {
        int iterations = 1000;
        this.fillMatrices();
        this.computeCriticalDelay();
        System.out.println("Critical delay: " + this.criticalDelay);

        double delayFactor = 1.001;
        double deviation = 5;
        do {
            System.out.println("running with delayFactor " + delayFactor);
            this.solveLinearEquationSystem(this.criticalDelay * delayFactor, iterations);
            deviation = this.calculateInputDrivenDeviation();
            System.out.println("deviation: " + deviation);
            delayFactor *= 1.001;
        } while (deviation > 1.0);
        this.setCapactiances();
        this.selectSizes();
    }

    private void fillMatrices() {
        int gateCount = this.cellInstances.size();
        staticLoadMatrix_b = MatrixUtils.createRealMatrix(gateCount, 1);
        effortMatrix_T = MatrixUtils.createRealMatrix(gateCount, gateCount);

        for (int i = 0; i < gateCount; i++) {
            List<Load> loads = cellInstances.get(i).getLoads();
            for (Load l : loads) {
                if (l.isStaticLoad()) {
                    double oldValue = staticLoadMatrix_b.getEntry(i, 0);
                    staticLoadMatrix_b.setEntry(i, 0, oldValue + l.getCapacitanceTheoretical());
                } else {
                    CellInstance loadInstance = l.getCellInstance();
                    int loadIndex = this.findGateIndex(loadInstance);
                    effortMatrix_T.setEntry(i, loadIndex, loadInstance.getDefinition().getLogicalEffortForPin(l.getPinName()));
                }
            }
            double oldValue = effortMatrix_T.getEntry(i, i);
            effortMatrix_T.setEntry(i, i, oldValue + cellInstances.get(i).getDefinition().getAvgParasiticDelay());
        }
    }

    private int findGateIndex(CellInstance aGateInstance) {
        for (int i = 0; i < this.cellInstances.size(); i++) {
            if (cellInstances.get(i) == aGateInstance) {
                return i;
            }
        }
        throw new Error("Could not find load CellInstance in this module");
    }

    private void computeCriticalDelay() {
        EigenDecomposition e = new EigenDecomposition(effortMatrix_T);
        double[] realParts = e.getRealEigenvalues();
        double[] imagParts = e.getImagEigenvalues();

        double largestAbsoluteEigenvalue = 0;
        for (int i = 0; i < realParts.length; i++) {
            if (imagParts[i] < 0.0000000001 & realParts[i] > largestAbsoluteEigenvalue) {
                largestAbsoluteEigenvalue = realParts[i];
            }
        }
        this.criticalDelay = largestAbsoluteEigenvalue;
    }

    private void solveLinearEquationSystem(double targetDelay, int iterations) {
        driveStrengthMatrix_x = MatrixUtils.createRealMatrix(this.cellInstances.size(), 1);

        for (int i = 0; i < iterations; i++) {
            RealMatrix effortLoadMatrix = this.effortMatrix_T.multiply(driveStrengthMatrix_x);
            driveStrengthMatrix_x = effortLoadMatrix.add(this.staticLoadMatrix_b).scalarMultiply(1 / targetDelay);
        }
    }

    @SuppressWarnings("unused")
    private void printX(RealMatrix x, int iteration) {
        for (int i = 0; i < x.getRowDimension(); i++) {
            double value = x.getEntry(i, 0);
            System.out.print(value);
            if ( i < x.getRowDimension() - 1 ){
                System.out.print(',');
            }
        }
        System.out.print('\n');
    }

    private double calculateInputDrivenDeviation() {
        double errorSum = 0.0;
        int inputDrivenCount = 0;
        for (int i = 0; i < this.cellInstances.size(); i++) {
            CellInstance cellInstance = this.cellInstances.get(i);
            if (cellInstance.isInputDriven()) {
                double pinSum = 0.0;
                for (String inputPinName : cellInstance.getInputPinNames()) {
                    double logicalEffort = cellInstance.getDefinition().getLogicalEffortForPin(inputPinName);
                    double newCapacitance = logicalEffort * this.driveStrengthMatrix_x.getEntry(i, 0);
                    double originalCapacitance = cellInstance.getInputPinTheoreticalCapacitance(inputPinName);
                    double error = newCapacitance / originalCapacitance;
                    pinSum += error;
                }
                double cellError = pinSum / cellInstance.getInputPinNames().size();
                if (cellError > 1.0) {
                    errorSum += cellError;
                    inputDrivenCount++;
                }
            }
        }
        if (inputDrivenCount == 0) {
            return 0;
        }
        return errorSum / inputDrivenCount;
    }

    private void setCapactiances() {
        for (int i = 0; i < this.cellInstances.size(); i++) {
            CellInstance cellInstance = this.cellInstances.get(i);
            for (String inputPinName : cellInstance.getInputPinNames()) {
                double logicalEffort = cellInstance.getDefinition().getLogicalEffortForPin(inputPinName);
                double capacitance = logicalEffort * this.driveStrengthMatrix_x.getEntry(i, 0);
                capacitance = Math.max(capacitance, 0.00000001);
                cellInstance.setInputPinTheoreticalCapacitance(inputPinName, capacitance, false);
            }
        }
    }

    private void selectSizes() {
        for (CellInstance i : this.cellInstances) {
            i.selectSizeFromTheoreticalCapacitances();
        }
    }

}
