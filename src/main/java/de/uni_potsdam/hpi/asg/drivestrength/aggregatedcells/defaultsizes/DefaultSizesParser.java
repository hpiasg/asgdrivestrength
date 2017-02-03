package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.defaultsizes;

import java.io.File;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.asg.drivestrength.util.FileHelper;

public class DefaultSizesParser {

    private File defaultSizesFile;
    
    public DefaultSizesParser(File defaultSizesFile) {
        this.defaultSizesFile = defaultSizesFile;
    }
    
    public DefaultSizesContainer run() {
        String json = FileHelper.readTextFileToString(defaultSizesFile);
        DefaultSizesContainer c = new Gson().fromJson(json, DefaultSizesContainer.class);
        return c;
    }
}
