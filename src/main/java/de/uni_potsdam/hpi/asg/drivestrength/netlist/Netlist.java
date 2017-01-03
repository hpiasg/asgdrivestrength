package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.ArrayList;
import java.util.List;

public class Netlist {
    private List<Module> modules;
    private Module rootModule;
    
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
    
    public void setModules(List<Module> newModules) {
        this.modules = newModules;
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
