package de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Module;

public class PredecessorAnnotator {
    private Module module;

    public PredecessorAnnotator(Netlist netlist) {
        this.module = netlist.getRootModule();
    }

    public void run() {
        for (CellInstance c : module.getCellInstances()) {
            for (Load load : c.getLoads()) {
                if (!load.isStaticLoad()) {
                    CellInstance successor = load.getCellInstance();
                    successor.addPredecessor(c);
                }
            }
        }
    }
}
