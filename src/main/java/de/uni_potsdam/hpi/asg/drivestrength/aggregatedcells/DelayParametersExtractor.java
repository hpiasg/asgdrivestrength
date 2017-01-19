package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
        List<DelayPoint> intersections = extractDelayLineIntersections();
        List<DelayPoint> middlePoints = calculateMiddlePointsBetween(intersections);
        
        /* let us define x := nth-root(H) and a := N * nth-root(G).
         * Thus, after transforming the points we can fit a linear function */
        
        List<DelayPoint> linearizedPoints = transformPointsToLinear(middlePoints);

        LinearFunctionFitter f = new LinearFunctionFitter(linearizedPoints);
        double parasiticDelay = f.getIntercept();
        double a = f.getSlope();
        double logicalEffort = Math.pow(a / stageCount, stageCount);

        return new DelayParameterTriple(logicalEffort, parasiticDelay, stageCount);
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

    private List<DelayPoint> extractDelayLineIntersections() {
        List<DelayPoint> intersections = new ArrayList<>();
        
        // heuristic: add additional point to the left
        intersections.add(new DelayPoint(0, this.findLowestDelayOffset()));
        
        for (int i = 0; i < delayLines.size() - 1; i++) {
            DelayPoint intersection = delayLines.get(i).getIntersectionPoint(delayLines.get(i+1));
            if (intersection.isPositive()) {
                intersections.add(intersection);
            }
        }
        
        if (intersections.size() < 2) {
            throw new Error("cannot extract multi-stage delay parameters with no intersection");
        }
        
        // heuristic: add additional point further to the right (on least-slope delayLine)
        double rightmostIntersectionX = intersections.get(intersections.size() - 1).getX();
        double deltaX = rightmostIntersectionX - intersections.get(intersections.size() - 2).getX();
        double newPointX = rightmostIntersectionX + deltaX;
        intersections.add(new DelayPoint(newPointX, delayLines.get(delayLines.size() - 1).valueAtX(newPointX)));
        
        return intersections;
    }
    
    private List<DelayPoint> calculateMiddlePointsBetween(List<DelayPoint> originalPoints) {
        List<DelayPoint> middlePoints = new ArrayList<>();
        for (int i = 0; i < originalPoints.size() - 1; i++) {
            DelayPoint middlePoint = DelayPoint.centerBetween(originalPoints.get(i), originalPoints.get(i+1));
            middlePoints.add(middlePoint);
        }
        return middlePoints;
    }

    private List<DelayPoint> transformPointsToLinear(List<DelayPoint> originalNthRootFunctionPoints) {
        List<DelayPoint> linearizedPoints = new ArrayList<>();
        for (DelayPoint p : originalNthRootFunctionPoints) {
            linearizedPoints.add(new DelayPoint(Math.pow(p.getElectricalEffort(), 1.0 / stageCount), p.getDelay()));
        }
        return linearizedPoints;        
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
}
