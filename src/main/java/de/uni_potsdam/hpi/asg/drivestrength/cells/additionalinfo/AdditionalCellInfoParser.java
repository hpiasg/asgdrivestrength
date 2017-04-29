package de.uni_potsdam.hpi.asg.drivestrength.cells.additionalinfo;

import java.io.File;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.asg.drivestrength.util.FileHelper;

public class AdditionalCellInfoParser {
    private File additionalInfoJsonFile;

    public AdditionalCellInfoParser(File additionalInfoJsonFile) {
        this.additionalInfoJsonFile = additionalInfoJsonFile;
    }

    public AdditionalCellInfoContainer run() {
        String json = FileHelper.readTextFileToString(additionalInfoJsonFile);
        AdditionalCellInfoContainer c = new Gson().fromJson(json, AdditionalCellInfoContainer.class);
        return c;
    }
}

