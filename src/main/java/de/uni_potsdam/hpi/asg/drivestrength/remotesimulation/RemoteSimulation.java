package de.uni_potsdam.hpi.asg.drivestrength.remotesimulation;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.asg.common.remote.RemoteInformation;
import de.uni_potsdam.hpi.asg.drivestrength.util.FileHelper;

public class RemoteSimulation {
    private String name;
    private String netlist;
    private File remoteConfigFile;    
    
    public RemoteSimulation(String name, String netlist, File remoteConfigFile) {
        this.name = name;
        this.netlist = netlist;
        this.remoteConfigFile = remoteConfigFile;
    }
    
    public int run() {
        String json = FileHelper.readTextFileToString(remoteConfigFile);
        RemoteInformation rinfo = new Gson().fromJson(json, RemoteConfig.class).asRemoteInformation();         
        
        DateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");                 
        String date = dfmt.format(new Date());
        
        SimulationRemoteOperationWorkflow wf = new SimulationRemoteOperationWorkflow(rinfo, name + "_" + date);

        Set<String> filesToMove = new HashSet<>();
        List<String> filesToExecute = new ArrayList<>();
        
        String tempDir = "tmp/" + date + "/";
        new File(tempDir).mkdir();
        
        String netlistFilename = tempDir + name + ".v";
        FileHelper.writeStringToTextFile(netlist, netlistFilename);
        filesToMove.add(netlistFilename);
        
        String commandFilename = tempDir + name + ".sh";
        FileHelper.writeStringToTextFile("simulate " + name + ".v " + name + " | grep -E 'ERROR|SUCCESS' > output.txt", commandFilename);
        filesToMove.add(commandFilename);
        filesToExecute.add(name + ".sh");
        
        wf.run(filesToMove, filesToExecute, tempDir, false);
        
        String output = FileHelper.readTextFileToString(new File(tempDir + "output.txt")).trim();
        FileHelper.deleteFileRecursive(new File(tempDir));

        Pattern simulationResultPattern = Pattern.compile("[0-9]* TB_SUCCESS:\\s*([0-9]*)");
        Matcher m = simulationResultPattern.matcher(output);
        
        if (m.matches()) {
            return Integer.parseInt(m.group(1));
        }
        throw new Error("Simulation unsuccessful: " + output);
    }
}
