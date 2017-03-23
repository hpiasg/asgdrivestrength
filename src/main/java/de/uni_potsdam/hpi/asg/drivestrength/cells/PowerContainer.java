package de.uni_potsdam.hpi.asg.drivestrength.cells;

public class PowerContainer {
    private String relatedPinName;
    private PowerMatrix risePower;
    private PowerMatrix fallPower;

    public PowerContainer() {
    }

    public PowerMatrix getRiseDelays() {
        return risePower;
    }

    public void setRisePower(PowerMatrix risePower) {
        this.risePower = risePower;
    }

    public PowerMatrix getFallDelays() {
        return fallPower;
    }

    public void setFallPower(PowerMatrix fallPower) {
        this.fallPower = fallPower;
    }


    public String getRelatedPinName() {
        return relatedPinName;
    }

    public void setRelatedPinName(String relatedPinName) {
        this.relatedPinName = relatedPinName;
    }

}
