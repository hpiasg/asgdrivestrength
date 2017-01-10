package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Signal {
    protected static final Logger logger = LogManager.getLogger();

    public enum Direction {
        input, output, wire, supply0, supply1, constant
    }

    protected String    name;
    protected Direction direction;
    protected int       width;


    private static Signal zeroInstance;

    public static Signal getZeroInstance() {
        if (zeroInstance == null) {
            zeroInstance = new Signal("0", Direction.constant, 1);
        }
        return zeroInstance;
    }
    
    public Signal(String name, Direction direction, int width) {
        this.direction = direction;
        this.name = name;
        this.width = width;
    }
    
    public Signal(Signal signalToCopy) {
        this.direction = signalToCopy.getDirection();
        this.name = signalToCopy.getName();
        this.width = signalToCopy.getWidth();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String newName) {
    	this.name = newName;
    }

    public int getWidth() {
        return width;
    }
    
    public boolean isBundle() {
        return width > 1;
    }

    @Override
    public String toString() {
        return name + ":" + direction + ",width:" + width;
    }
    
    public String toVerilog() {
        String directionString = "";
        switch (this.direction) {
            case input:
                directionString = "input";
                break;
            case output:
                directionString = "output";
                break;
            case wire:
                directionString = "wire";
                break;
            case supply0:
                directionString = "supply0";
                break;
            case supply1:
                directionString = "supply1";
                break;
            default:
                throw new Error("Trying to serialize Signal with unknown direction type: " + directionString);
        }
        String bundleString = "";
        if (width > 1) {
            bundleString = " [" + Integer.toString(width - 1) + ":0]";
        }
        return directionString + bundleString + " " + this.name + ";";
    }

    public Direction getDirection() {
        return direction;
    }
    
    public boolean isIOSignal() {
    	return (direction == Direction.input || direction == Direction.output);
    }
}
