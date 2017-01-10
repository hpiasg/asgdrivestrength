package de.uni_potsdam.hpi.asg.drivestrength.netlist.flattener;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.ModuleInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;

public class NetlistFlattener {
    protected static final Logger logger = LogManager.getLogger();
    
    private Netlist netlist;
    private List<Module> flattenedModules;

    public NetlistFlattener(Netlist netlist) {
        this.netlist = netlist;
    }
    
    public void run() {
        this.flattenedModules = new ArrayList<Module>();
        
        this.flattenFromModule(netlist.getRootModule());
        this.flattenedModules.add(netlist.getRootModule());
        netlist.setModules(this.flattenedModules);
    }
    
    private void addModuleUnique(Module module) {
        if (!this.flattenedModules.contains(module)) {
            this.flattenedModules.add(module);
        }
    }
    
    private void flattenFromModule(Module module) {
        for (ModuleInstance instance : module.getModuleInstances()) {
            Module definition = instance.getDefinition();
            if (definition.hasAssignStatementsOnly()) {
                addModuleUnique(definition);
                continue;
            }
            Module flattenedModule = this.createFlattenedCopy(definition, 
                                                 instance.getName(), module.getName());
            instance.setDefinition(flattenedModule);
            flattenFromModule(flattenedModule);
            // add *after* recursive call to preserve hierarchical ordering
            flattenedModules.add(flattenedModule); 
        }
    }
    
    private Module createFlattenedCopy(Module originalModule, String instanceName, String parentName) {
        Module module = new Module(originalModule);
        module.setName(parentName + "__" + module.getName() + "___" + instanceName);
        return module;
    }
    
}
