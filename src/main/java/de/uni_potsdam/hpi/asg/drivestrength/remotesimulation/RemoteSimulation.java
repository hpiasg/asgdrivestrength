package de.uni_potsdam.hpi.asg.drivestrength.remotesimulation;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

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
    
    public void run() {
        String json = FileHelper.readTextFile(remoteConfigFile);
        RemoteInformation rinfo = new Gson().fromJson(json, RemoteConfig.class).asRemoteInformation();         
        
        DateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");                 
        String date = dfmt.format(new Date());
        
        SimulationRemoteOperationWorkflow wf = new SimulationRemoteOperationWorkflow(rinfo, date);
        wf.run(new HashSet<>(), new ArrayList<>());
        
        System.out.println(date);
    }
}
