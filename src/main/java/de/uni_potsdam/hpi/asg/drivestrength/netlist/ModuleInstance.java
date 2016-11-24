package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.List;

public class ModuleInstance extends AbstractInstance {
    private Module definition;
    
    public ModuleInstance(String name, Module definition, List<PinConnection> pinConnections) {
        super(name, pinConnections);
        this.definition = definition;
    }

    @Override
    String definitionName() {
        return definition.getName();
    }
}
