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
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.util.FileHelper;
import de.uni_potsdam.hpi.asg.drivestrength.util.NumberFormatter;

public class RemoteSimulation {
    protected static final Logger logger = LogManager.getLogger();
    private static final Pattern simulationResultSuccessPattern = Pattern.compile("[0-9]*\\s*TB_SUCCESS:\\s*([0-9]*)");
    private static final Pattern totalPowerPattern = Pattern.compile("Total Power\\s* = ([0-9|\\-|e|\\.]+)\\s*.*");
    private static final Pattern simulationTimePattern = Pattern.compile("(.*)at time ([0-9]*) PS(.*)");

    //Simulation complete via $finish(1) at time 77210 PS + 0

    private String name;
    private Netlist netlist;
    private File remoteConfigFile;
    private boolean keepTempDir;
    private boolean verbose;
    private double outputPinCapacitance;
    private String tempDir;
    private RemoteSimulationResult remoteSimulationResult;

    public RemoteSimulation(Netlist netlist, File remoteConfigFile,
            double outputPinCapacitance, boolean keepTempDir, boolean verbose) {
        this.netlist = netlist;
        this.name = netlist.getName();
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
        FileHelper.writeStringToTextFile(netlist.toVerilog(), netlistFilename);
        filesToMove.add(netlistFilename);

        String commandFilename = tempDir + name + ".sh";
        String command = "";
        for (String librarySuffix : librarySuffixes) {
            command += "selectLibrary " + librarySuffix + "\n";
            command += "simulate " + name + ".v " + name + " " + outputPinCapacitance + " > output_full" + librarySuffix + ".txt;\n";
            command += "cat output_full" + librarySuffix + ".txt | grep -E 'ERROR|SUCCESS' > output_tb" + librarySuffix + ".txt;\n";
            command += "cat output_full" + librarySuffix + ".txt | grep -E 'Total Power' > output_power" + librarySuffix + ".txt;\n";
            command += "cat output_full" + librarySuffix + ".txt | grep -E 'Simulation complete' > output_simtime" + librarySuffix + ".txt;\n";
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

        parseTotalPowerAndSimTime(librarySuffixes[0]);

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
        File resultFile = new File(tempDir + "output_tb" + librarySuffix + ".txt");
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

    private void parseTotalPowerAndSimTime(String librarySuffix) {
        File resultFilePower = new File(tempDir + "output_power" + librarySuffix + ".txt");
        String resultPower = FileHelper.readTextFileToString(resultFilePower).split("\r\n|\r|\n")[0].trim();
        Matcher mPower = totalPowerPattern.matcher(resultPower);
        if (mPower.matches()) {
            File resultFileSimTime = new File(tempDir + "output_simtime" + librarySuffix + ".txt");
            String resultSimTime = FileHelper.readTextFileToString(resultFileSimTime).split("\r\n|\r|\n")[0].trim();
            Matcher mSimTime = simulationTimePattern.matcher(resultSimTime);
            if (mSimTime.matches()) {
                double totalPower = Double.parseDouble(mPower.group(1));
                int simTime = Integer.parseInt(mSimTime.group(2));
                double energy = totalPower * simTime;
                logger.info("Testbench total energy: " + energy);
                remoteSimulationResult.setTotalEnergy(energy);
                return;
            }
        }
        logger.warn("No total energy from testbench Simulation");
        remoteSimulationResult.setTotalEnergy(0.0);
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
