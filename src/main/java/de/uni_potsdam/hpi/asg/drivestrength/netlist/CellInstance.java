package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;

public class CellInstance extends AbstractInstance {
    
    private AggregatedCell definition;
    private String definitionName;
    private Map<String, Double> inputPinCapacitances;
    private CellInstance avatar; //the CellInstance this one was copied from (if copy was called accordingly). Capacitance setter also modifies avatar

    public CellInstance(String name, AggregatedCell definition, List<PinAssignment> pinAssignments) {
        super(name, pinAssignments);
        this.definition = definition;
        initializeInputPinCapacitances();
        this.nameAllPinAssignments();
    }
    public CellInstance(String name, String definitionName, List<PinAssignment> pinAssignments) {
        super(name, pinAssignments);
        this.definitionName = definitionName;
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
    
    private void initializeInputPinCapacitances() {
        this.inputPinCapacitances = new HashMap<>();
        for (String pinName : this.getInputPinNames()) {
            this.inputPinCapacitances.put(pinName, 0.0);
        }
    }

    @Override
    String getDefinitionName() {
        if (definitionName != null) {
            return definitionName;
        }
        return definition.getSizeNameFor(this.getAverageInputPinCapacitance());
    }
    
    public AggregatedCell getDefinition() {
        return definition;
    }
    
    public void setInputPinCapacitance(String inputPin, double newInputPinCapacitance) {
        this.inputPinCapacitances.put(inputPin, newInputPinCapacitance);
        if (this.avatar != null) {
            this.avatar.setInputPinCapacitance(inputPin, newInputPinCapacitance);
        }
    }
    
    public double getAverageInputPinCapacitance() {
        double sum = 0.0;
        for (double c : this.inputPinCapacitances.values()) {
            sum += c;
        }
        return sum / this.inputPinCapacitances.size();
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
    
    public boolean isInputSignal(Signal aSignal) {
        for (PinAssignment p : this.getPinAssignments()) {
            if (p.getSignal() == aSignal && !p.getPinName().equals(this.definition.getOutputPinName())) {
                return true;                    
            }
        }
        return false;
    }
    
    public String pinNameForConnectedSignal(Signal aSignal) {
        for (PinAssignment p : this.getPinAssignments()) {
            if (p.getSignal() == aSignal) {
                return p.getPinName();
            }
        }
        throw new Error("Signal " + aSignal.getName() + " is not connected to CellInstance " + this.getName());
    }

}
