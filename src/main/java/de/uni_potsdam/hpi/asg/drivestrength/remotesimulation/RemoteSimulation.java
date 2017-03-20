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
    private static final Pattern simulationResultSuccessPattern = Pattern.compile("[0-9]*\\s*TB_SUCCESS:\\s*([0-9]*)");

    private String name;
    private String netlist;
    private File remoteConfigFile;
    private boolean keepTempDir;
    private boolean verbose;
    private double outputPinCapacitance;
    private String tempDir;
    private RemoteSimulationResult remoteSimulationResult;

    public RemoteSimulation(File netlistfile, String netlist, File remoteConfigFile,
            double outputPinCapacitance, boolean keepTempDir, boolean verbose) {
        this(FileHelper.basename(netlistfile.getName()), netlist, remoteConfigFile, outputPinCapacitance, keepTempDir, verbose);
    }

    public RemoteSimulation(String netlistName, String netlist, File remoteConfigFile,
            double outputPinCapacitance, boolean keepTempDir, boolean verbose) {
        this.name = netlistName;
        this.netlist = netlist;
        this.remoteConfigFile = remoteConfigFile;
        this.outputPinCapacitance = outputPinCapacitance;
        this.keepTempDir = keepTempDir;
        this.verbose = verbose;
    }

    public void run() {
        if (remoteConfigFile == null) {
            logger.info("Skipping Remote Simulation (no remoteConfig file specified)");
            return;
        }

        logger.info("Starting remote simulation, with testbench " + this.name + "...");

        this.remoteSimulationResult = new RemoteSimulationResult();

        String[] librarySuffixes = {"_orig", "_noslew", "_noslew_nowire"};

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
        String command = "";
        for (String librarySuffix : librarySuffixes) {
            command += "selectLibrary " + librarySuffix + "\n";
            command += "simulate " + name + ".v " + name + " " + outputPinCapacitance
                    + " > output_full.txt; cat output_full.txt | grep -E 'ERROR|SUCCESS' > output" + librarySuffix + ".txt;";
            command += "cp simulation_" + name + "/" + name + ".sdf ./" + name + librarySuffix + ".sdf\n";
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

        for (String librarySuffix : librarySuffixes) {
            parseTBSuccess(librarySuffix);
            parseSdf(librarySuffix);
        }

        if (!this.keepTempDir) {
            FileHelper.deleteDirectory(tempDir);
        }
    }

    public RemoteSimulationResult getResult() {
        return this.remoteSimulationResult;
    }

    private String date() {
        DateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return dfmt.format(new Date());
    }

    private void parseTBSuccess(String librarySuffix) {
        File resultFile = new File(tempDir + "output" + librarySuffix + ".txt");
        String result = FileHelper.readTextFileToString(resultFile).split("\r\n|\r|\n")[0].trim();
        Matcher m = simulationResultSuccessPattern.matcher(result);

        if (m.matches()) {
            int runtime = Integer.parseInt(m.group(1));
            logger.info("Testbench Success (" + librarySuffix + ") after " + NumberFormatter.spaced(runtime) + " ps");
            remoteSimulationResult.addTestbenchSuccessTime(librarySuffix, runtime);
        } else {
            logger.info("Simulation result (" + librarySuffix + "): " + result);
            remoteSimulationResult.addTestbenchSuccessTime(librarySuffix, 0);
        }
    }

    private void parseSdf(String librarySuffix) {
        DelayFileParser sdfParser = new DelayFileParser(new File(tempDir + name + librarySuffix + ".sdf"));
        sdfParser.parse();
        if (this.verbose) {
            sdfParser.printAll();
        }
        remoteSimulationResult.addSdfDelaySum(librarySuffix, sdfParser.getDelaySum());
        logger.info("SDF cell delay sum (" + librarySuffix + "): " + NumberFormatter.spaced(sdfParser.getDelaySum()) + " ps");
    }
}
