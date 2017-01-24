package de.uni_potsdam.hpi.asg.drivestrength.cells;

import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Pin.Direction;

public class Cell {

    private String name;
    private String footprint;
    
    private final List<Pin> pins;
    
    public Cell() {
        this.pins = new ArrayList<Pin>();
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

    public List<Pin> getPins() {
        return pins;
    }

    public void addPin(Pin pin) {
        this.pins.add(pin);
    }

    public String getFootprint() {
        return footprint;
    }

    public void setFootprint(String footprint) {
        this.footprint = footprint;
    }

    public Pin getOutputPin() {
        for (Pin pin : this.pins) {
            if (pin.getDirection() == Direction.output) {
                return pin;
            }
        }
        throw(new Error("Could not find output pin for cell " + this.name));
    }
    
    public boolean hasSingleOutputPin() {
        int outputPinCount = 0;
        for (Pin pin : this.pins) {
            if (pin.getDirection() == Direction.output) {
                outputPinCount++;
            }
        }
        return outputPinCount == 1;
    }
    
    public List<Pin> getInputPins() {
    	List<Pin> inputPins = new ArrayList<Pin>();
        for (Pin pin : this.pins) {
            if (pin.getDirection() == Direction.input) {
            	inputPins.add(pin);
            }
        }
        return inputPins;
    }
    
    public boolean hasClockPin() {
		for (Pin pin: this.pins) {
			if (pin.isClockPin()) return true;
		}
	    return false;
	}
    
    public double getCapacitanceForPin(String pinName) {
        for (Pin pin : this.pins) {
            if (pin.getName().equals(pinName)) {
                return pin.getCapacitance();
            }
        }
        throw new Error("Could not find capacitance for pin " + pinName + " in rawCell " + this.getName());
    }
}
