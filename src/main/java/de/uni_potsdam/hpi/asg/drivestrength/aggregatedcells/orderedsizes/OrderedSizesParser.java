package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.orderedsizes;

import java.io.File;
import java.util.List;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.asg.drivestrength.util.FileHelper;

public class OrderedSizesParser {
    private File orderedSizesFile;
    private boolean skipDeviatingSizes;
    private List<String> deviatingSizes;

    public OrderedSizesParser(File defaultSizesFile, boolean skipDeviatingSizes, List<String> deviatingSizes) {
        this.orderedSizesFile = defaultSizesFile;
        this.skipDeviatingSizes = skipDeviatingSizes;
        this.deviatingSizes = deviatingSizes;
    }

    public OrderedSizesContainer run() {
        String json = FileHelper.readTextFileToString(orderedSizesFile);
        OrderedSizesContainer c = new Gson().fromJson(json, OrderedSizesContainer.class);

        if (this.skipDeviatingSizes) {
            for (String deviatingSize : this.deviatingSizes) {
                c.removeSize(deviatingSize);
            }
        }

        return c;
    }
}
