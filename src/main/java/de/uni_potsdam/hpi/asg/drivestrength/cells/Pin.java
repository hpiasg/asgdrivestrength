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
    private List<Timing> timings;
    

    public Pin() {
        this.timings = new ArrayList<>();
    }
    
    
    public List<Timing> getTimings() {
        return timings;
    }
    
    public void addTiming(Timing timing) {
        this.timings.add(timing);
    }

    public double getCapacitance() {
        return capacitance;
    }

    public void setCapacitance(double capacitance) {
        this.capacitance = capacitance;
    }

    
    public boolean hasTimings() {
        return (this.timings != null && this.timings.size() > 0);
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
}
