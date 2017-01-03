package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;

public class GateInstance extends AbstractInstance {
    
    private AggregatedCell definition;
    private double inputPinCapacitance;
    
    public GateInstance(String name, AggregatedCell definition, List<PinAssignment> pinAssignments) {
        super(name, pinAssignments);
        this.definition = definition;
        this.inputPinCapacitance = 1.0;
    }

    @Override
    String getDefinitionName() {
        return definition.getSizeNameFor(this.inputPinCapacitance);
    }
    
    AggregatedCell getDefinition() {
        return definition;
    }

}
