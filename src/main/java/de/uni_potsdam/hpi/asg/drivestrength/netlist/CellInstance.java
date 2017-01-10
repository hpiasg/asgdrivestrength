package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;

public class CellInstance extends AbstractInstance {
    
    private AggregatedCell definition;
    private double inputPinCapacitance;
    
    public CellInstance(String name, AggregatedCell definition, List<PinAssignment> pinAssignments) {
        super(name, pinAssignments);
        this.definition = definition;
        this.inputPinCapacitance = 100.0;
    }

    @Override
    String getDefinitionName() {
        return definition.getSizeNameFor(this.inputPinCapacitance);
    }
    
    public AggregatedCell getDefinition() {
        return definition;
    }

}
