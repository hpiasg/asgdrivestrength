package de.uni_potsdam.hpi.asg.drivestrength.cells;

public class TimingContainer {
    private String relatedPinName;
    private DelayMatrix7x7 riseDelays;
    private DelayMatrix7x7 fallDelays;
    
    public TimingContainer() {
    }
    
    public DelayMatrix7x7 getRiseDelays() {
        return riseDelays;
    }

    public void setRiseDelays(DelayMatrix7x7 riseDelays) {
        this.riseDelays = riseDelays;
    }

    public DelayMatrix7x7 getFallDelays() {
        return fallDelays;
    }

    public void setFallDelays(DelayMatrix7x7 fallDelays) {
        this.fallDelays = fallDelays;
    }


    public String getRelatedPinName() {
        return relatedPinName;
    }

    public void setRelatedPinName(String relatedPinName) {
        this.relatedPinName = relatedPinName;
    }
    
}
