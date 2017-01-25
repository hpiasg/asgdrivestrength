package de.uni_potsdam.hpi.asg.drivestrength.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHelper {
    public static String readTextFile(File textFile) {
        try {
            byte[] fileContents = Files.readAllBytes(Paths.get(textFile.getPath()));
            return new String(fileContents);
        } catch (IOException e) {
            throw new Error("Could not read file " +  textFile);
        }
    }
}
