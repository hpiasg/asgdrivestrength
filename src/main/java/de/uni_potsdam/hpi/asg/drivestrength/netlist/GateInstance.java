package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.List;

public class GateInstance extends AbstractInstance {
    
    private String definition;  /*TODO: After we have library parser, replace with reference to Gate */
    
    public GateInstance(String name, String definition, List<PinConnection> pinConnections) {
        super(name, pinConnections);
        this.definition = definition;
    }

    @Override
    String definitionName() {
        return definition;
    }

}
