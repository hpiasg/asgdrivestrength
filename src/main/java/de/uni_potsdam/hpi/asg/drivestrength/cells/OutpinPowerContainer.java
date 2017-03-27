package de.uni_potsdam.hpi.asg.drivestrength.cells;

public class OutpinPowerContainer {
    private String relatedPinName;
    private PowerMatrix risePower;
    private PowerMatrix fallPower;

    public OutpinPowerContainer() {
    }

    public PowerMatrix getRisePower() {
        return risePower;
    }

    public void setRisePower(PowerMatrix risePower) {
        this.risePower = risePower;
    }

    public PowerMatrix getFallPower() {
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
