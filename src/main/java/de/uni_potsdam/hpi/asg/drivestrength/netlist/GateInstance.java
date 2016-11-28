package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.List;

public class GateInstance extends AbstractInstance {
    
    private String definition;  /*TODO: After we have library parser, replace with reference to Gate */
    
    public GateInstance(String name, String definition, List<PinAssignment> pinAssignments) {
        super(name, pinAssignments);
        this.definition = definition;
    }

    @Override
    String getDefinitionName() {
        return definition;
    }

}
