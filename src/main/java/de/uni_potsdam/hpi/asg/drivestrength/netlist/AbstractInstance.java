package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInstance {
    private String name;
    private List<PinConnection> pinConnections;
    
    public AbstractInstance(String name, List<PinConnection> pinConnections) {
        this.name = name;
        this.pinConnections = pinConnections;
    }
    
    abstract String definitionName();
    
    String toVerilog() {
        String verilog = definitionName() + " " + this.getName() + " (";
        List<String> pinConnectionLiterals = new ArrayList<>();
        for (PinConnection pc : this.getPinConnections()) {
            pinConnectionLiterals.add(pc.toVerilog());
        }
        verilog += String.join(", ", pinConnectionLiterals);
        verilog += ");";
        return verilog;
    }

    public String getName() {
        return name;
    }

    public List<PinConnection> getPinConnections() {
        return pinConnections;
    }
}
