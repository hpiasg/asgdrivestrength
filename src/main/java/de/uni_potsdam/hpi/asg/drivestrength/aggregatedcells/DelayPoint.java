package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

public class DelayPoint {
    private double electricalEffort;
    private double delay;
    
    public DelayPoint(double electricalEffort, double delay) {
        this.electricalEffort = electricalEffort;
        this.delay = delay;
    }
    
    public static DelayPoint centerBetween(DelayPoint a, DelayPoint b) {
        double electricalEffort = (a.getElectricalEffort() + b.getElectricalEffort()) / 2;
        double delay = (a.getDelay() + b.getDelay()) / 2;
        return new DelayPoint(electricalEffort, delay);
    }
    
    public double getX() {
        return electricalEffort;
    }
    
    public double getY() {
        return delay;
    }
    
    public double getElectricalEffort() {
        return electricalEffort;
    }
    
    public double getDelay() {
        return delay;
    }
    
    public String toString() {
        return "(" + electricalEffort + ", " + delay + ")";
    }
    
    public boolean isPositive() {
        return (electricalEffort > 0 && delay > 0);
    }
}
