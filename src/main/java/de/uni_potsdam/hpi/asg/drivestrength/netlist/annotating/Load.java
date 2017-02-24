package de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;

public class Load {
    private CellInstance cellInstance;
    private String pinName;
    private double staticCapacitance;
    
    public Load(CellInstance cellInstance, String pinName) {
        this.cellInstance = cellInstance;
        this.pinName = pinName;
    }
    
    public Load(double staticCapacitance) {
        this.staticCapacitance = staticCapacitance;
    }
    
    public double getCapacitanceTheoretical() {
        if (this.isStaticLoad()) {
            return this.staticCapacitance;
        }
        return this.cellInstance.getInputPinTheoreticalCapacitance(pinName);
    }
    
    public double getCapacitanceSelected() {
        if (this.isStaticLoad()) {
            return this.staticCapacitance;
        }
        return this.cellInstance.getInputPinSelectedCapacitance(pinName);
    }
    
    public boolean isStaticLoad() {
        return this.cellInstance == null;
    }

    public CellInstance getCellInstance() {
        return cellInstance;
    }

    public String getPinName() {
        return pinName;
    }
    
    
}
