package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.ModuleInstance;

public class Netlist {
    private List<Module> modules;
    private Module rootModule;
    private String name;

    public Netlist() {
        modules = new ArrayList<>();
    }

    public Netlist(Netlist netlistToCopy) {
        modules = new ArrayList<>();
        Module oldRootModule = netlistToCopy.getRootModule();
        for (Module m : netlistToCopy.getModules()) {
            Module copiedModule = new Module(m, true);
            modules.add(copiedModule);
            if (m == oldRootModule) {
                this.setRootModule(copiedModule);
            }
        }
        this.name = netlistToCopy.name;
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

    public boolean isFlat() {
    	List<Module> instanciatedModuleDefinitions = new ArrayList<Module>();
    	for (Module module : this.getModules()) {
    		for (ModuleInstance i : module.getModuleInstances()) {
    			Module definition = i.getDefinition();
    			if (instanciatedModuleDefinitions.contains(definition) && !definition.hasAssignStatementsOnly()) {
    				return false;
    			}
    			instanciatedModuleDefinitions.add(i.getDefinition());
    		}
    	}
    	return true;
    }

    public boolean isInlined() {
        return this.getModules().size() == 1;
    }

    public boolean isAllSingleStage() {
        for (Module m : this.getModules()) {
            for (CellInstance i: m.getCellInstances()) {
                if (!(i.getDefinition().isSingleStageCell())) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
