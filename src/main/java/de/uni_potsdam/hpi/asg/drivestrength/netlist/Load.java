package de.uni_potsdam.hpi.asg.drivestrength.netlist;

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
    
    public double getCapacitance() {
        if (this.cellInstance == null) {
            return this.staticCapacitance;
        }
        return this.cellInstance.getInputPinCapacitance(pinName);
    }
}
