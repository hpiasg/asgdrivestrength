package de.uni_potsdam.hpi.asg.drivestrength.netlist.flattener;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.ModuleInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;

public class NetlistFlattener {
    private static final Logger logger = LogManager.getLogger();
    
    private Netlist netlist;
    private List<Module> flattenedModules;

    public NetlistFlattener(Netlist netlist) {
        this.netlist = netlist;
    }
    
    public Netlist run() {
        this.flattenFromModule(netlist.getRootModule());
        
        return this.netlist;
    }
    
    private void flattenFromModule(Module module) {
        logger.info("flatten from module "+ module.getName());
        
        for (ModuleInstance instance : module.getModuleInstances()) {
            logger.info("instance of module " + instance.getDefinitionModule().getName());
            this.createFlattenedCopy(instance.getDefinitionModule(), instance.getName());
        }
    }
    
    private void createFlattenedCopy(Module module, String instanceName) {
        Module moduleCopy = module.copy();
    }
    
}
