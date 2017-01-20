package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells;

public class DelayParameterTriple {
    private double logicalEffort;
    private double parasiticDelay;
    private int stageCount;
    
    public DelayParameterTriple(double logicalEffort, double parasiticDelay, int stageCount) {
        this.logicalEffort = logicalEffort;
        this.parasiticDelay = parasiticDelay;
        this.stageCount = stageCount;
    }

    public double getLogicalEffort() {
        return logicalEffort;
    }

    public double getParasiticDelay() {
        return parasiticDelay;
    }

    public int getStageCount() {
        return stageCount;
    }
}
