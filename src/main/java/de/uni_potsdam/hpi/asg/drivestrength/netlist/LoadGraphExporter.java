package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.annotating.Load;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.CellInstance;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.elements.Module;

public class LoadGraphExporter {

    protected static final Logger logger = LogManager.getLogger();

    private Module module;
    private String nodesJson;
    private String linksJson;
    private boolean useTheoreticalLoad;

    public LoadGraphExporter(Netlist netlist, boolean useTheoreticalLoad) {
        this.module= netlist.getRootModule();
        this.nodesJson = "";
        this.linksJson = "";
        this.useTheoreticalLoad = useTheoreticalLoad;
    }

    public void print() {
        if (this.useTheoreticalLoad) {
            logger.info("Load graph (from theoretical sizes): ");
        } else {
            logger.info("Load graph (from chosen sizes): ");
        }

        System.out.println(this.run());
    }

    public String run() {
        int staticLoadId = 0;
        for (CellInstance c : module.getCellInstances()) {
            this.nodesJson +=  makeNodeJson(id(c), this.findAppropriateCapacitance(c), c.isInputDriven());
            for (Load l : c.getLoads()) {
                if (l.isStaticLoad()) {
                    this.nodesJson += makeNodeJson("staticLoad"+staticLoadId, l.getCapacitanceTheoretical(), false);
                    this.linksJson += makeLinkJson(id(c), "staticLoad"+staticLoadId, "static", true);
                    staticLoadId++;
                } else {
                    this.linksJson += makeLinkJson(id(c), id(l.getCellInstance()), l.getPinName(), true);
                }
            }

        }
        nodesJson = nodesJson.substring(0, nodesJson.length()-1);
        linksJson = linksJson.substring(0, linksJson.length()-1);

        return "{\"nodes\": [" + this.nodesJson + "], \n\"links\": [" + this.linksJson + "]}";
    }

    private double findAppropriateCapacitance(CellInstance c) {
        if (this.useTheoreticalLoad) {
            return c.getAverageInputPinTheoreticalCapacitance();
        }
        return c.getAverageInputPinSelectedCapacitance();
    }

    private String makeNodeJson(String id, double capacitance, boolean isInputDriven) {
        return "{\"id\":\"" + id + "\", \"avgCapacitance\": " + capacitance + ", \"isInputDriven\":" + isInputDriven + "},";
    }

    private String makeLinkJson(String sourceId, String targetId, String targetPinName, boolean isStatic) {
        return "{\"source\":\"" + sourceId + "\", \"target\": \"" + targetId + "\", \"targetPinName\":\"" + targetPinName + "\"},";
    }

    private String id(CellInstance c) {
        return c.getDefinitionName() + " " + c.getName();
    }
}
