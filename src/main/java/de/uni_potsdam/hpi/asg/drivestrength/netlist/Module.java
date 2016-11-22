package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser.VerilogModuleParser;

public class Module {

    private String name;
    private List<String> interfaceSignals;
    private List<Signal> signals;

    public static Module newFromVerilog(List<String> verilogStatements) {
        VerilogModuleParser moduleParser = new VerilogModuleParser(verilogStatements);
        return moduleParser.createModule();
    }
    
    public Module() {
        this.name = null;
        this.signals = new ArrayList<Signal>();
        this.interfaceSignals = new ArrayList<String>();
    }
    
    public String toVerilog() {
        String verilog = "module " + this.name + "(";
        verilog += String.join(", ", this.interfaceSignals);
        verilog += ");\n";
        
        for (Signal signal : this.signals) {
            verilog += "  " + signal.toVerilog() + "\n";
        }
        
        verilog += "\n";
        verilog += "  //TODO: other module contents\n";
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
}
