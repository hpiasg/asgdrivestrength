package de.uni_potsdam.hpi.asg.drivestrength.cells.additionalinfo;

import java.io.File;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.asg.drivestrength.util.FileHelper;

public class AdditionalCellInfoParser {

    private boolean skipDeviatingSizes;
    private File additionalInfoJsonFile;
    
    public AdditionalCellInfoParser(File additionalInfoJsonFile, boolean skipDeviatingSizes) {
        this.additionalInfoJsonFile = additionalInfoJsonFile;
        this.skipDeviatingSizes = skipDeviatingSizes;
    }
    
    public AdditionalCellInfoContainer run() {
        String json = FileHelper.readTextFileToString(additionalInfoJsonFile);
        AdditionalCellInfoContainer c = new Gson().fromJson(json, AdditionalCellInfoContainer.class);
        
        if (this.skipDeviatingSizes) {
            for (String deviatingSize : c.listDeviatingSizes()) {
                c.removeOrderedSize(deviatingSize);
            }
        }
        
        return c;
    }
}

