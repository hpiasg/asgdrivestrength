package de.uni_potsdam.hpi.asg.drivestrength.cells;

public class InpinPowerContainer {
    private PowerMatrix7x1 risePower;
    private PowerMatrix7x1 fallPower;

    public InpinPowerContainer() {
    }

    public PowerMatrix7x1 getRisePower() {
        return risePower;
    }

    public void setRisePower(PowerMatrix7x1 risePower) {
        this.risePower = risePower;
    }

    public PowerMatrix7x1 getFallPower() {
        return fallPower;
    }

    public void setFallPower(PowerMatrix7x1 fallPower) {
        this.fallPower = fallPower;
    }
}
