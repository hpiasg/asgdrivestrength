package de.uni_potsdam.hpi.asg.drivestrength.netlist;

public class Load {
    private CellInstance cellInstance;
    private String pinName;
    
    public Load(CellInstance cellInstance, String pinName) {
        this.cellInstance = cellInstance;
        this.pinName = pinName;
    }
    
    public double getCapacitance() {
        return this.cellInstance.getInputPinCapacitance(pinName);
    }
}
