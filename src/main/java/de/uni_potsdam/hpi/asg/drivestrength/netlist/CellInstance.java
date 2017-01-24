package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;

public class CellInstance extends AbstractInstance {
    
    private AggregatedCell definition;
    private String definitionName;
    private double inputPinCapacitance;
    private CellInstance avatar; //the CellInstance this one was copied from (if copy was called accordingly). Capacitance setter also modifies avatar

    public CellInstance(String name, AggregatedCell definition, List<PinAssignment> pinAssignments) {
        super(name, pinAssignments);
        this.definition = definition;
        this.inputPinCapacitance = 0;
        this.nameAllPinAssignments();
    }
    public CellInstance(String name, String definitionName, List<PinAssignment> pinAssignments) {
        super(name, pinAssignments);
        this.definitionName = definitionName;
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
        if (definitionName != null) {
            return definitionName;
        }
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
    
    private void nameAllPinAssignments() {
        if (this.definition == null) {
            throw new Error("Cannot name positional pin assignments for dummy cell instance " + this.getName());
        }
        for (PinAssignment p : this.getPinAssignments()) {
            if (p.isPositional()) {
                String pinName = this.definition.getPinNameAtPosition(p.getPinPosition());
                p.setPinName(pinName);
            }
        }
    }
    
    public Signal getOutputSignal() {
        if (this.definition == null) {
            throw new Error("Cannot find output Signal for dummy cell instance " + this.getName());
        }
        String outputPinName = this.definition.getOutputPinName();
        for (PinAssignment p : this.getPinAssignments()) {
            if (p.getPinName().equals(outputPinName)) {
                return p.getSignal();                    
            }
        }
        throw new Error("Cannot find output Signal for cell instance " + this.getName());
    }
    
    public List<String> getInputPinNames() {
        if (this.definition == null) {
            throw new Error("Cannot find input pin names for dummy cell instance " + this.getName());
        }
        return this.definition.getInputPinNames();
    }
    
    public Signal getInputSignal(String inputPinName) {
        if (this.definition == null) {
            throw new Error("Cannot find input Signal for dummy cell instance " + this.getName());
        }
        for (PinAssignment p : this.getPinAssignments()) {
            if (p.getPinName().equals(inputPinName)) {
                return p.getSignal();                    
            }
        }
        throw new Error("Cannot find input Signal for cell instance " + this.getName());
    }

}
