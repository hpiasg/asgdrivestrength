package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.aggregators;

import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.DelayLine;
import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.DelayPoint;

public class LinearFunctionFitter {
    SimpleRegression apacheSimpleRegression;
            
    public LinearFunctionFitter(List<DelayPoint> delayPoints) {
        if (delayPoints.size() < 2) {
            throw new Error("cannot fit linear function with just one data point");
        }
        apacheSimpleRegression = new SimpleRegression();
        
        for (DelayPoint p : delayPoints) {
            apacheSimpleRegression.addData(p.getX(), p.getY());
        }
        
        if (delayPoints.size() == 2) {
            DelayPoint center = DelayPoint.centerBetween(delayPoints.get(0), delayPoints.get(1));
            apacheSimpleRegression.addData(center.getX(), center.getY());
        }
        apacheSimpleRegression.regress();
    }
    
    public double getSlope() {
        return apacheSimpleRegression.getSlope();
    }
    
    public double getIntercept() {
        return apacheSimpleRegression.getIntercept();
    }
    
    public DelayLine getDelayLine() {
        return new DelayLine(this.getSlope(), this.getIntercept());
    }
}
