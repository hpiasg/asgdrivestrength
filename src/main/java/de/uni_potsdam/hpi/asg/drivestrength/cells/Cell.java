package de.uni_potsdam.hpi.asg.drivestrength.cells;

import java.util.ArrayList;
import java.util.List;

public class Cell {

    private String name;
    private List<Pin> pins;
    
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
    
}
