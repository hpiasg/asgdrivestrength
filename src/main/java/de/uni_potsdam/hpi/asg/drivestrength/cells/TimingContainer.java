package de.uni_potsdam.hpi.asg.drivestrength.cells;

public class TimingContainer {
    private String relatedPinName;
    private DelayMatrix riseDelays;
    private DelayMatrix fallDelays;
    
    public TimingContainer() {
    }
    
    public DelayMatrix getRiseDelays() {
        return riseDelays;
    }

    public void setRiseDelays(DelayMatrix riseDelays) {
        this.riseDelays = riseDelays;
    }

    public DelayMatrix getFallDelays() {
        return fallDelays;
    }

    public void setFallDelays(DelayMatrix fallDelays) {
        this.fallDelays = fallDelays;
    }


    public String getRelatedPinName() {
        return relatedPinName;
    }

    public void setRelatedPinName(String relatedPinName) {
        this.relatedPinName = relatedPinName;
    }
    
}
