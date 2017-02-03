package de.uni_potsdam.hpi.asg.drivestrength.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHelper {
    public static String readTextFileToString(File textFile) {
        try {
            byte[] fileContents = Files.readAllBytes(Paths.get(textFile.getPath()));
            return new String(fileContents);
        } catch (IOException e) {
            throw new Error("Could not read file " +  textFile);
        }
    }
    
    public static void writeStringToTextFile(String fileContent, String fileName) {
        try {
            PrintWriter out = new PrintWriter(fileName);
            out.print(fileContent);
            out.close();
        } catch (IOException e) {
            throw new Error("Could not write file " + fileName);
        }
    }
    
    public static void deleteFileRecursive(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
                return;
            }
            if (file.isDirectory()) {
                String files[] = file.list();
                for (String str : files) {
                    deleteFileRecursive(new File(file, str));
                }
                file.delete();
            }
        }
    }
}
