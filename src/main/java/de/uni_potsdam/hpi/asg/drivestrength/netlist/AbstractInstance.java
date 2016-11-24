package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInstance {
    private String name;
    private List<PinAssignment> pinAssignments;
    
    public AbstractInstance(String name, List<PinAssignment> pinAssignments) {
        this.name = name;
        this.pinAssignments = pinAssignments;
    }
    
    abstract String definitionName();
    
    String toVerilog() {
        String verilog = definitionName() + " " + this.getName() + " (";
        List<String> pinAssignmentLiterals = new ArrayList<>();
        for (PinAssignment pc : this.getPinConnections()) {
            pinAssignmentLiterals.add(pc.toVerilog());
        }
        verilog += String.join(", ", pinAssignmentLiterals);
        verilog += ");";
        return verilog;
    }

    public String getName() {
        return name;
    }

    public List<PinAssignment> getPinConnections() {
        return pinAssignments;
    }
}
