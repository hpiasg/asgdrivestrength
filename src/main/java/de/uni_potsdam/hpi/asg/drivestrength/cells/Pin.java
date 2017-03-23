package de.uni_potsdam.hpi.asg.drivestrength.cells;

import java.util.ArrayList;
import java.util.List;

public class Pin {
    public enum Direction {
        input, output, inout, internal
    }

    private String name;
    private Direction direction;
    private double capacitance;
    private final List<TimingContainer> timings;
    private final List<PowerContainer> powerContainers;
    private boolean isClockPin;


    public Pin() {
        this.timings = new ArrayList<>();
        this.powerContainers = new ArrayList<>();
        this.isClockPin = false;
    }

    public List<TimingContainer> getTimings() {
        return timings;
    }

    public void addTiming(TimingContainer timing) {
        this.timings.add(timing);
    }

    public List<PowerContainer> getPowerContainers() {
        return this.powerContainers;
    }

    public void addPowerContainer(PowerContainer powerContainer) {
        this.powerContainers.add(powerContainer);
    }

    public double getCapacitance() {
        return capacitance;
    }

    public void setCapacitance(double capacitance) {
        this.capacitance = capacitance;
    }

    public void markAsClockPin() {
    	this.isClockPin = true;
    }

    public boolean hasTimings() {
        return (this.timings != null && this.timings.size() > 0);
    }

    public boolean hasPowerContainers() {
        return (this.powerContainers != null && this.powerContainers.size() > 0);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getName() {
        return this.name;
    }


	public boolean isClockPin() {
		return isClockPin;
	}


}
