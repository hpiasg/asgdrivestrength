package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.orderedsizes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderedSizesContainer {

    private HashMap<String, ArrayList<String>> orderedSizes;

    public OrderedSizesContainer() {
    }

    public ArrayList<String> get(String footprint) {
        return orderedSizes.get(footprint);
    }

    public void removeSize(String size) {
        for (List<String> sizes: orderedSizes.values()) {
            sizes.remove(size);
        }
    }

    @Override
    public String toString() {
        return orderedSizes.toString();
    }
}
