package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.orderedsizes;

import java.io.File;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.asg.drivestrength.util.FileHelper;

public class OrderedSizesParser {
    private File orderedSizesFile;

    public OrderedSizesParser(File defaultSizesFile) {
        this.orderedSizesFile = defaultSizesFile;
    }

    public OrderedSizesContainer run() {
        String json = FileHelper.readTextFileToString(orderedSizesFile);
        OrderedSizesContainer c = new Gson().fromJson(json, OrderedSizesContainer.class);
        return c;
    }
}
