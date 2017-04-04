package de.uni_potsdam.hpi.asg.drivestrength.netlist.elements;

public class EstimatorCache {
    private boolean delayInvalidated;
    private boolean energyInvalidated;
    private double delayValue;
    private double energyValue;

    public EstimatorCache() {
        this.delayInvalidated = true;
        this.energyInvalidated = true;
    }

    public boolean isDelayInvalidated() {
        return this.delayInvalidated;
    }

    public boolean isEnergyInvalidated() {
        return this.energyInvalidated;
    }

    public double getDelayValue() {
        return delayValue;
    }

    public void setDelayValue(double delayValue) {
        this.delayValue = delayValue;
        this.delayInvalidated = false;
    }

    public double getEnergyValue() {
        return energyValue;
    }

    public void setEnergyValue(double energyValue) {
        this.energyValue = energyValue;
        this.energyInvalidated = false;
    }

    public void invalidate() {
        this.energyInvalidated = true;
        this.delayInvalidated = true;
    }
}
