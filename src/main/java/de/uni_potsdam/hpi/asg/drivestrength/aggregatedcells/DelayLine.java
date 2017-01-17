package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

import java.util.List;

public class DelayLine {
    private double slope;
    private double offset;
    
    public DelayLine(double slope, double offset) {
        this.slope = slope;
        this.offset = offset;
    }
    
    public static DelayLine averageFrom(DelayLine a, DelayLine b) {
        double slope = (a.getSlope() + b.getSlope()) / 2;
        double offset = (a.getOffset() + b.getOffset()) / 2;
        return new DelayLine(slope, offset);
    }
    
    public static DelayLine averageFrom(List<DelayLine> delayLines) {
        double slopeSum = 0;
        double offsetSum = 0;
        double count = delayLines.size();
        for (DelayLine line : delayLines) {
            slopeSum += line.getSlope();
            offsetSum += line.getOffset();
        }
        return new DelayLine(slopeSum / count, offsetSum / count);
    }
    
    public double getIntersectionX(DelayLine other) {
        return (other.getOffset() - this.offset) / (this.slope - other.getSlope());
    }
    
    public DelayPoint getIntersectionPoint(DelayLine other) {
        double x = this.getIntersectionX(other);
        return new DelayPoint(x, this.valueAtX(x));
    }
    
    public double valueAtX(double x) {
        return this.offset + this.slope * x;
    }

    public double getSlope() {
        return slope;
    }

    public double getOffset() {
        return offset;
    }
    
    public String toString() {
        return "DelayLine(slope=" + slope + ", offset=" + offset + ")";
    }
}
