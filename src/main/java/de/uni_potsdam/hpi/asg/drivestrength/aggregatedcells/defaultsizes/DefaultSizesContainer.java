package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.defaultsizes;

import java.util.HashMap;

public class DefaultSizesContainer {

    private HashMap<String, String> defaultSizes;
    
    public DefaultSizesContainer() {
    }
    
    public String get(String footprint) {
        return defaultSizes.get(footprint);
    }
    
}
