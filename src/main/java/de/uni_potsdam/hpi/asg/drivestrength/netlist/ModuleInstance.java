package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.List;

public class ModuleInstance extends AbstractInstance {
    private Module definition;
    
    public ModuleInstance(String name, Module definition, List<PinAssignment> pinAssignments) {
        super(name, pinAssignments);
        this.definition = definition;
    }

    @Override
    String getDefinitionName() {
        return definition.getName();
    }
    
    public Module getDefinitionModule() {
        return this.definition;
    }
}
