package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.ArrayList;
import java.util.List;

public class Module {

    private String name;
    private List<String> interfaceSignals;
    private List<Signal> signals;
    private List<AssignConnection> assignConnections;
    private List<AbstractInstance> instances; /*these are instances of gates and *other* modules */
    
    public Module() {
        this.name = null;
        this.signals = new ArrayList<>();
        this.interfaceSignals = new ArrayList<>();
        this.assignConnections = new ArrayList<>();
        this.instances = new ArrayList<>();
    }
    
    public String toVerilog() {
        String verilog = "module " + this.name + "(";
        verilog += String.join(", ", this.interfaceSignals);
        verilog += ");\n";
        
        for (Signal signal : this.signals) {
            verilog += "  " + signal.toVerilog() + "\n";
        }
        
        if(this.assignConnections.size() > 0) {
            verilog += "\n";            
        }
        
        for (AssignConnection assignConnection: this.assignConnections) {
            verilog += "  " + assignConnection.toVerilog() + "\n";
        }

        if(this.instances.size() > 0) {
            verilog += "\n";            
        }
        
        for (AbstractInstance instance: this.instances) {
            verilog += "  " + instance.toVerilog() + "\n";
        }
        
        verilog += "endmodule";
        return verilog; 
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void addInterfaceSignal(String interfaceSignal) {
        this.interfaceSignals.add(interfaceSignal);
    }
    
    public void addSignal(Signal signal) {
        this.signals.add(signal);
    }
    
    public void addInstance(AbstractInstance instance) {
        this.instances.add(instance);
    }
    
    public Signal getSignalByName(String signalName) {
        if (signalName.equals("gnd")) {
            return Signal.getGroundInstance();
        }
        if (signalName.equals("vdd")) {
            return Signal.getSupplyInstance();
        }
        if (signalName.equals("0")) {
            return Signal.getZeroInstance();
        }
        for (Signal s : this.signals) {
            if (s.getName().equals(signalName)) {
                return s;
            }
        }
        throw new Error("Module does not have a Signal named " + signalName);
    }
    
    public void addAssignConnection(AssignConnection assignConnection) {
        this.assignConnections.add(assignConnection);
    }
}
