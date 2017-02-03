package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;

public class CellInstance extends AbstractInstance {
    
    private AggregatedCell definition;
    private String definitionName;
    private Map<String, Double> inputPinTheoreticalCapacitances;
    private CellInstance avatar; //the CellInstance this one was copied from (if copy was called accordingly). Capacitance setter also modifies avatar
    private List<Load> loads;
    private Cell selectedSize;

    public CellInstance(String name, AggregatedCell definition, List<PinAssignment> pinAssignments) {
        super(name, pinAssignments);
        this.definition = definition;
        initializeInputPinTheoreticalCapacitances();
        this.nameAllPinAssignments();
        this.loads = new ArrayList<>();
        this.selectedSize = definition.getDefaultSize();
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
    
    public void addLoad(Load aLoad) {
        this.loads.add(aLoad);
    }
    
    public List<Load> getLoads() {
        return this.loads;
    }
    
    public double getLoadCapacitanceTheoretical() {
        double totalCapacitance = 0.0;
        for (Load l : this.loads) {
            totalCapacitance += l.getCapacitanceTheoretical();
        }
        return totalCapacitance;
    }
    
    public double getLoadCapacitanceSelected() {
        double totalCapacitance = 0.0;
        for (Load l : this.loads) {
            totalCapacitance += l.getCapacitanceSelected();
        }
        return totalCapacitance;
    }
    
    private void initializeInputPinTheoreticalCapacitances() {
        this.inputPinTheoreticalCapacitances = new HashMap<>();
        for (String pinName : this.getInputPinNames()) {
            this.inputPinTheoreticalCapacitances.put(pinName, this.definition.getDefaultCapacitanceForPin(pinName));
        }
    }

    @Override
    String getDefinitionName() {
        if (definitionName != null) {
            return definitionName;
        }
        return selectedSize.getName();
    }
    
    public void selectSizeFromTheoreticalCapacitances() {
        this.selectedSize = definition.getSizeForInputCapacitances(this.inputPinTheoreticalCapacitances);
    }
    
    public void selectSizeForLoad(double loadCapacitance) {
        this.selectedSize = definition.getFastestSizeForLoad(loadCapacitance);
    }
    
    public AggregatedCell getDefinition() {
        return definition;
    }
    
    public void setInputPinTheoreticalCapacitance(String inputPin, double newInputPinCapacitance, boolean clampToPossible) {
        if (clampToPossible) {
            newInputPinCapacitance = Math.min(newInputPinCapacitance, this.definition.getLargestPossibleCapacitance(inputPin));
            newInputPinCapacitance = Math.max(newInputPinCapacitance, this.definition.getSmallestPossibleCapacitance(inputPin));
        }
        this.inputPinTheoreticalCapacitances.put(inputPin, newInputPinCapacitance);
        if (this.avatar != null) {
            this.avatar.setInputPinTheoreticalCapacitance(inputPin, newInputPinCapacitance, false);
        }
    }
    
    public double getInputPinTheoreticalCapacitance(String inputPinName) {
        return this.inputPinTheoreticalCapacitances.get(inputPinName);
    }
    
    public double getInputPinSelectedCapacitance(String inputPinName) {
        return this.definition.getSizeCapacitance(this.selectedSize.getName(), inputPinName);
    }
    
    public double getAverageInputPinTheoreticalCapacitance() {
        double sum = 0.0;
        for (double c : this.inputPinTheoreticalCapacitances.values()) {
            sum += c;
        }
        return sum / this.inputPinTheoreticalCapacitances.size();
    }
    
    public double getAverageInputPinSelectedCapacitance() {
        double sum = 0.0;
        int count = 0;
        for (String inputPinName : this.getInputPinNames()) {
            sum += this.getInputPinSelectedCapacitance(inputPinName);
            count++;
        }
        return sum / count;
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
    
    public boolean isDummyCellInstance() {
        return (this.definition == null);
    }

}
