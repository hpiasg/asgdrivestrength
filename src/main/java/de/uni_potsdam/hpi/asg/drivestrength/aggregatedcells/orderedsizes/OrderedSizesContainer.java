package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.orderedsizes;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderedSizesContainer {

    private HashMap<String, ArrayList<String>> orderedSizes;

    public OrderedSizesContainer() {
    }

    public ArrayList<String> get(String footprint) {
        return orderedSizes.get(footprint);
    }

    public String toString() {
        return orderedSizes.toString();
    }
}
