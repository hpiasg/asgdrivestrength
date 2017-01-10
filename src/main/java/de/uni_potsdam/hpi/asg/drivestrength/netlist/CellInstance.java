package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;

public class CellInstance extends AbstractInstance {
    
    private AggregatedCell definition;
    private double inputPinCapacitance;
    private CellInstance avatar; //the CellInstance this one was copied from (if copy was called accordingly). Capacitance setter also modifies avatar
    
    public CellInstance(String name, AggregatedCell definition, List<PinAssignment> pinAssignments) {
        super(name, pinAssignments);
        this.definition = definition;
        this.inputPinCapacitance = 0;
    }
    
    public void setAvatar(CellInstance avatar) {
        this.avatar = avatar;
    }
    
    public CellInstance getAvatarOrSelf() {
        if (this.avatar == null)
            return this;
        else
            return this.avatar;
    }

    @Override
    String getDefinitionName() {
        return definition.getSizeNameFor(this.inputPinCapacitance);
    }
    
    public AggregatedCell getDefinition() {
        return definition;
    }
    
    public void setInputPinCapacitance(double newInputPinCapacitance) {
        this.inputPinCapacitance = newInputPinCapacitance;
        if (this.avatar != null) {
            this.avatar.setInputPinCapacitance(newInputPinCapacitance);
        }
    }

}
