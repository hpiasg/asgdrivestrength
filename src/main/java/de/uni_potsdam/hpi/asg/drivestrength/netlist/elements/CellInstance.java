package de.uni_potsdam.hpi.asg.drivestrength.netlist.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.Load;

public class CellInstance extends AbstractInstance {

    private AggregatedCell definition;
    private String definitionName;
    private Map<String, Double> inputPinTheoreticalCapacitances;
    private CellInstance avatar; //the CellInstance this one was copied from (if copy was called accordingly). Capacitance setter also modifies avatar
    private List<Load> loads;
    private Set<CellInstance> predecessors;
    private Cell selectedSize;
    private boolean isInputDriven;
    private double inputDrivenMaxCIn;
    private EstimatorCache estimatorCache;

    public CellInstance(String name, AggregatedCell definition, List<PinAssignment> pinAssignments) {
        super(name, pinAssignments);
        this.definition = definition;
        initializeInputPinTheoreticalCapacitances();
        this.nameAllPinAssignments();
        this.loads = new ArrayList<>();
        this.selectedSize = definition.getDefaultSize();
        this.isInputDriven = false;
        this.predecessors = new HashSet<>();
        this.estimatorCache = new EstimatorCache();
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

    public void markAsInputDriven(double inputDrivenMaxCIn) {
        this.isInputDriven = true;
        this.inputDrivenMaxCIn = inputDrivenMaxCIn;
    }

    public void addPredecessor(CellInstance newPredecessor) {
        predecessors.add(newPredecessor);
    }

    public List<CellInstance> getPredecessors() {
        return new ArrayList<CellInstance>(predecessors);
    }

    public boolean isInputDriven() {
        return this.isInputDriven;
    }

    public void addLoad(Load aLoad) {
        this.loads.add(aLoad);
    }

    public void clearLoads() {
        this.loads = new ArrayList<>();
    }

    public List<Load> getLoads() {
        return this.loads;
    }

    public List<CellInstance> getSuccessors() {
        List<CellInstance> successors = new ArrayList<>();
        for (Load load : this.getLoads()) {
            if (!load.isStaticLoad()) {
                successors.add(load.getCellInstance());
            }
        }
        return successors;
    }

    public boolean hasSuccessors() {
        return this.getSuccessors().size() > 0;
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
    public String getDefinitionName() {
        if (definitionName != null) {
            return definitionName;
        }
        return selectedSize.getName();
    }

    public void selectSizeFromTheoreticalCapacitances() {
        this.selectedSize = definition.getSizeForInputCapacitances(this.inputPinTheoreticalCapacitances);
        this.invalidateCache();
        if (this.avatar != null) {
            this.avatar.selectSizeFromTheoreticalCapacitances();
        }
    }

    private void invalidateCache() {
        this.getEstimatorCache().invalidate();
        for (CellInstance p : this.predecessors) {
            p.getEstimatorCache().invalidate();
        }
    }

    public void selectRandomSize() {
        this.selectSize(definition.getRandomSize());
    }

    public void selectNextBiggerSizeIfPossible() {
        this.selectSize(this.definition.getNextBiggerSizeTo(this.selectedSize));
    }

    public void selectNextSmallerSizeIfPossible() {
        this.selectSize(this.definition.getNextSmallerSizeTo(this.selectedSize));
    }

    public void selectSize(Cell sizeToSelect) {
        if (this.isInputDriven && sizeToSelect.violatesMaxCIn(inputDrivenMaxCIn)) return;
        this.selectedSize = sizeToSelect;
        this.invalidateCache();
        if (this.avatar != null) {
            this.avatar.selectSize(this.selectedSize);
        }
    }

    public Cell getSelectedSize() {
        return this.selectedSize;
    }

    public void selectFastestSizeForLoad(double loadCapacitance) {
        this.selectSize(definition.getFastestSizeForLoad(loadCapacitance));
    }

    public AggregatedCell getDefinition() {
        return definition;
    }

    public void setInputPinTheoreticalCapacitance(String inputPin, double newInputPinCapacitance, boolean clampToPossible) {
        if (this.isInputDriven) {
            newInputPinCapacitance = Math.min(newInputPinCapacitance, this.inputDrivenMaxCIn);
        }
        if (clampToPossible) {
            newInputPinCapacitance = Math.min(newInputPinCapacitance, this.definition.getLargestPossibleCapacitance(inputPin));
            newInputPinCapacitance = Math.max(newInputPinCapacitance, this.definition.getSmallestPossibleCapacitance(inputPin));
        }
        this.inputPinTheoreticalCapacitances.put(inputPin, newInputPinCapacitance);
        this.invalidateCache();
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

    public boolean hasPredecessors() {
        return this.predecessors.size() > 0;
    }

    public String toString() {
        return this.getName();
    }

    public EstimatorCache getEstimatorCache() {
        return estimatorCache;
    }

}
