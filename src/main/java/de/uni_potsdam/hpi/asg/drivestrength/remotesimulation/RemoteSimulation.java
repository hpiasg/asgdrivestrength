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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.asg.common.remote.RemoteInformation;
import de.uni_potsdam.hpi.asg.drivestrength.delayfiles.DelayFileParser;
import de.uni_potsdam.hpi.asg.drivestrength.util.FileHelper;
import de.uni_potsdam.hpi.asg.drivestrength.util.NumberFormatter;

public class RemoteSimulation {
    protected static final Logger logger = LogManager.getLogger();
    private static final Pattern simulationResultSuccessPattern = Pattern.compile("[0-9]* TB_SUCCESS:\\s*([0-9]*)");

    private String name;
    private String netlist;
    private File remoteConfigFile;
    private boolean includeSdf;
    private String tempDir;

    public RemoteSimulation(String filename, String netlist, File remoteConfigFile, boolean includeSdf) {
        this.name = basename(filename);
        this.netlist = netlist;
        this.remoteConfigFile = remoteConfigFile;
        this.includeSdf = includeSdf;
    }

    private String basename(String filename) {
        return filename.split("\\.(?=[^\\.]+$)")[0];
    }

    public void run() {
        if (remoteConfigFile == null) {
            logger.info("Skipping Remote Simulation (no remoteConfig file specified)");
            return;
        }

        logger.info("Starting remote simulation, with testbench " + this.name + "...");


        String date = date();
        tempDir = "tmp/" + date + "/";
        new File(tempDir).mkdir();

        String json = FileHelper.readTextFileToString(remoteConfigFile);
        RemoteInformation remoteInfo = new Gson().fromJson(json, RemoteConfig.class).asRemoteInformation();


        Set<String> filesToMove = new HashSet<>();
        List<String> filesToExecute = new ArrayList<>();

        String netlistFilename = tempDir + name + ".v";
        FileHelper.writeStringToTextFile(netlist, netlistFilename);
        filesToMove.add(netlistFilename);

        String commandFilename = tempDir + name + ".sh";
        String command = "simulate " + name + ".v " + name + " | grep -E 'ERROR|SUCCESS' > output.txt;";
        if (includeSdf) {
            command += "cp simulation_" + name + "/" + name + ".sdf .";
        }
        FileHelper.writeStringToTextFile(command, commandFilename);
        filesToMove.add(commandFilename);
        filesToExecute.add(name + ".sh");

        SimulationRemoteOperationWorkflow workFlow = new SimulationRemoteOperationWorkflow(remoteInfo, name + "_" + date);
        boolean success = workFlow.run(filesToMove, filesToExecute, tempDir, true);
        if (!success) {
            FileHelper.deleteDirectory(tempDir);
            throw new Error("Remote Simulation failed");
        }

        parseResult();
        if (this.includeSdf) {
            parseSdf();
        }

        //FileHelper.deleteDirectory(tempDir);
    }

    private String date() {
        DateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return dfmt.format(new Date());
    }

    private void parseResult() {
        File resultFile = new File(tempDir + "output.txt");
        String result = FileHelper.readTextFileToString(resultFile).trim();
        Matcher m = simulationResultSuccessPattern.matcher(result);

        if (m.matches()) {
            int runtime = Integer.parseInt(m.group(1));
            logger.info("Testbench Success after " + NumberFormatter.spaced(runtime) + " ps");
        } else {
            logger.info("Simulation result: " + result);
        }
    }

    private void parseSdf() {
        DelayFileParser sdfParser = new DelayFileParser(new File(tempDir + name + ".sdf"));
        sdfParser.parse();
        sdfParser.printAll();
        logger.info("SDF cell delay sum: " + NumberFormatter.spaced(sdfParser.getDelaySum()) + " ps");
    }
}
