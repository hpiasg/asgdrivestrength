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
    private double criticalDelay;


    public EqualDelayMatrixOptimizer(Netlist netlist) {
        this.cellInstances = netlist.getRootModule().getCellInstances();
        //TODO: assert that all gates are single-stage
    }

    public void run() {
        this.fillMatrices();
        this.computeCriticalDelay();
        this.solveLinearEquationSystem();

        System.out.println("Critical Delay: " + criticalDelay);
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
                    effortMatrix_T.setEntry(i, loadIndex, loadInstance.getDefinition().getAvgLogicalEffort());
                }
            }
            double oldValue = effortMatrix_T.getEntry(i, i);
            effortMatrix_T.setEntry(i, i, oldValue + cellInstances.get(i).getDefinition().getAvgParasiticDelay());
        }
    }

    private int findGateIndex(CellInstance aGateInstance) {
        //TODO: precalculate gate->index map if this turns out to be slow
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
        this.criticalDelay = largestAbsoluteEigenvalue * 1.01;
    }

    private void solveLinearEquationSystem() {
        RealMatrix driveStrengthMatrix_x = MatrixUtils.createRealMatrix(this.cellInstances.size(), 1);
        System.out.println(driveStrengthMatrix_x);

        int iterations = 500;

        for (int i = 0; i < iterations; i++) {
            RealMatrix effortLoadMatrix = this.effortMatrix_T.multiply(driveStrengthMatrix_x);
            driveStrengthMatrix_x = effortLoadMatrix.add(this.staticLoadMatrix_b).scalarMultiply(1 / this.criticalDelay);
            printX(driveStrengthMatrix_x, iterations - 1);
        }
    }

    private void printX(RealMatrix x, int iteration) {
        for (int i = 0; i < x.getRowDimension(); i++) {
            double value = x.getEntry(i, 0);
            //System.out.print(Math.pow(value, 1.0 / iteration));
            System.out.print(value);
            if ( i < x.getRowDimension() - 1 ){
                System.out.print(',');
            }
        }
        System.out.print('\n');
    }


}
