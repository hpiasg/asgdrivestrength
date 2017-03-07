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


    public EqualDelayMatrixOptimizer(Netlist netlist) {
        this.cellInstances = netlist.getRootModule().getCellInstances();
        //TODO: assert that all gates are single-stage
    }

    public void run() {
        this.fillMatrices();
        double criticalDelay = this.computeCriticalDelay();

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

    private double computeCriticalDelay() {
        EigenDecomposition e = new EigenDecomposition(effortMatrix_T);
        double[] realParts = e.getRealEigenvalues();
        double[] imagParts = e.getImagEigenvalues();

        double largestAbsoluteEigenvalue = 0;
        for (int i = 0; i < realParts.length; i++) {
            if (imagParts[i] < 0.0000000001 & realParts[i] > largestAbsoluteEigenvalue) {
                largestAbsoluteEigenvalue = realParts[i];
            }
        }
        return largestAbsoluteEigenvalue;
    }
}
