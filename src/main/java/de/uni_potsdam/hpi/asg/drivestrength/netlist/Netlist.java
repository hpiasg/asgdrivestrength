package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser.VerilogParser;

public class Netlist {
    private List<Module> modules;
    private Module rootModule;
    
    public static Netlist newFromVerilog(File verilogFile) {
        VerilogParser p = new VerilogParser(verilogFile);
        return p.createNetlist();
    }
    
    public static Netlist newFromVerilog(List<String> statements) {
        VerilogParser p = new VerilogParser(statements);
        return p.createNetlist();
    }
    
    public Netlist() {
        modules = new ArrayList<Module>();
    }
    
    public List<Module> getModules() {
        return modules;
    }

    public Module getRootModule() {
        return rootModule;
    }
    
    public void setRootModule(Module newRootModule) {
        this.rootModule = newRootModule;
    }
    
    public void addModule(Module newModule) {
        modules.add(newModule);
        this.rootModule = newModule;
    }
    
    public Module getModuleByName(String moduleName) {

        for (Module m : this.modules) {
            if (m.getName().equals(moduleName)) {
                return m;
            }
        }
        throw new Error("Netlist does not have a Module named " + moduleName);
    }
    
    public String toVerilog() {
        String verilog = "";
        for (Module module: modules) {
            verilog += module.toVerilog();
            verilog += "\n\n";
        }
        return verilog;
    }
}
