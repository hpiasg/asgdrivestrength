package de.uni_potsdam.hpi.asg.drivestrength.netlist;

public class LoadGraphExporter {
    private Module module;
    private String nodesJson;
    private String linksJson;
    
    public LoadGraphExporter(Netlist netlist) {
        this.module= netlist.getRootModule();
        this.nodesJson = "";
        this.linksJson = "";
    }
    
    public String run() {
        int staticLoadId = 0;
        for (CellInstance c : module.getCellInstances()) {
            this.nodesJson +=  makeNodeJson(id(c), c.getAverageInputPinCapacitance());
            for (Load l : c.getLoads()) {
                if (l.isStaticLoad()) {
                    this.nodesJson += makeNodeJson("staticLoad"+staticLoadId, c.getAverageInputPinCapacitance());
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
    
    private String makeNodeJson(String id, double capacitance) {
        return "{\"id\":\"" + id + "\", \"avgCapacitance\": " + capacitance + "},";
    }
    
    private String makeLinkJson(String sourceId, String targetId, String targetPinName, boolean isStatic) {
        return "{\"source\":\"" + sourceId + "\", \"target\": \"" + targetId + "\", \"targetPinName\":\"" + targetPinName + "\"},";
    }
    
    private String id(CellInstance c) {
        return c.getDefinitionName() + " " + c.getName();
    }
}
