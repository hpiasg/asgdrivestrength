package de.uni_potsdam.hpi.asg.drivestrength.cells;

public class OutpinPowerContainer {
    private String relatedPinName;
    private PowerMatrix7x7 risePower;
    private PowerMatrix7x7 fallPower;

    public OutpinPowerContainer() {
    }

    public PowerMatrix7x7 getRisePower() {
        return risePower;
    }

    public void setRisePower(PowerMatrix7x7 risePower) {
        this.risePower = risePower;
    }

    public PowerMatrix7x7 getFallPower() {
        return fallPower;
    }

    public void setFallPower(PowerMatrix7x7 fallPower) {
        this.fallPower = fallPower;
    }


    public String getRelatedPinName() {
        return relatedPinName;
    }

    public void setRelatedPinName(String relatedPinName) {
        this.relatedPinName = relatedPinName;
    }

}
