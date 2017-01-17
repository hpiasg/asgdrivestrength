package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public class DelayParametersExtractor {
    private List<DelayLine> delayLines;
    private int stageCount;

    public DelayParametersExtractor(Map<String, DelayLine> delayLines, int stageCount) {
        this.delayLines = new ArrayList<DelayLine>(delayLines.values());
        this.stageCount = stageCount;
    }

    public DelayParameterTriple run() {
        if (stageCount == 1) {
            DelayLine averageDelayLine = DelayLine.averageFrom(delayLines);
            return new DelayParameterTriple(averageDelayLine.getSlope(), averageDelayLine.getOffset(), stageCount);
        }
        
        sortDelayLinesBySlope();
        
        List<DelayPoint> intersections = new ArrayList<>();
        intersections.add(new DelayPoint(0, findLowestDelayOffset()));
        for (int i = 0; i < delayLines.size() - 1; i++) {
            DelayPoint intersection = delayLines.get(i).getIntersectionPoint(delayLines.get(i+1));
            if (intersection.isPositive()) {
                System.out.println("intersection at " + intersection);
                intersections.add(intersection);
            }
        }
        
        List<DelayPoint> middlePoints = new ArrayList<>();
        for (int i = 0; i < intersections.size() - 1; i++) {
            DelayPoint middlePoint = DelayPoint.centerBetween(intersections.get(i), intersections.get(i+1));
            middlePoints.add(middlePoint);
        }
        System.out.println("Middle points: " + middlePoints);

        /* let us define x := nth-root(H) and a := N * nth-root(G) */
        //Transform x values with new x := nth-root(H) so the data becomes linear
        List<DelayPoint> linearizedPoints = new ArrayList<>();
        for (DelayPoint p : middlePoints) {
            linearizedPoints.add(new DelayPoint(Math.pow(p.getElectricalEffort(), 1.0 / stageCount), p.getDelay()));
        }
        System.out.println("Lizearized: " + linearizedPoints);
        SimpleRegression s = new SimpleRegression();
        for (DelayPoint p : linearizedPoints) {
            s.addData(p.getX(), p.getY());
        }
        s.regress();
        double a = s.getSlope();
        double parasiticDelay = s.getIntercept();
        System.out.println("estimated function:" + a + " * x + " + parasiticDelay);
        double logicalEffort = Math.pow(a / stageCount, stageCount);

        System.out.println("estimated delay: " + stageCount + " * (" + logicalEffort + " * H)^(1.0/" + stageCount + ") + " + parasiticDelay);
        
        return new DelayParameterTriple(logicalEffort, parasiticDelay, stageCount);
    }
    
    private double findLowestDelayOffset() {
        double lowestOffset = Double.MAX_VALUE;
        for (DelayLine delayLine : delayLines) {
            if (delayLine.getOffset() < lowestOffset) {
                lowestOffset = delayLine.getOffset();
            }
        }
        return lowestOffset;
    }
    
    private void sortDelayLinesBySlope() {
        Collections.sort(delayLines, new Comparator<DelayLine>() {
            public int compare(DelayLine a, DelayLine b) {
                if (a.getSlope() == b.getSlope()) return 0;
                if (a.getSlope() < b.getSlope()) return 1;
                return -1;
            }
        });
    }

    
    private double nthRootValue(int stageCount, double parasiticDelay, double logicalEffort, double electricalEffort) {
        return parasiticDelay + stageCount * Math.pow(logicalEffort * electricalEffort, stageCount);
    }
}
