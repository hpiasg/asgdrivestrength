package de.uni_potsdam.hpi.asg.drivestrength.netlist.loadAnnotator;

import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Load;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal;

public class LoadAnnotator {
    private List<CellInstance> cellInstances;
    
    public LoadAnnotator(Netlist netlist) {
        this.cellInstances = netlist.getRootModule().getCellInstances();
    }
    
    public void run() {
        this.addLoadsToCellInstances();
    }
    
    private void addLoadsToCellInstances() {
        for (CellInstance cellInstance : this.cellInstances) {
            Signal signal = cellInstance.getOutputSignal();
            for (CellInstance c : this.cellInstances) {
                if (c.isInputSignal(signal)) {
                    cellInstance.addLoad(new Load(c, c.pinNameForConnectedSignal(signal)));
                }
            }
            //TODO: assigns, input, output, constants
        }
    }
}  
