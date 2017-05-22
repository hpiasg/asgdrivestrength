package de.uni_potsdam.hpi.asg.drivestrength.gui;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractParameters.GeneralTextParam;
import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractRunner;
import de.uni_potsdam.hpi.asg.drivestrength.DrivestrengthGuiMain;
import de.uni_potsdam.hpi.asg.drivestrength.gui.DrivestrengthParameters.IntParam;
import de.uni_potsdam.hpi.asg.drivestrength.gui.DrivestrengthParameters.TextParam;

public class DrivestrengthRunner extends AbstractRunner {
    private DrivestrengthParameters params;

    public DrivestrengthRunner(DrivestrengthParameters params) {
        super(params);
        this.params = params;
    }

    public void run(TerminalMode mode) {
        this.run(mode, null);
    }

    public void run(TerminalMode mode, Window parent) {
        List<String> cmd = buildCmd();
        exec(cmd, "ASGdrivestrength terminal", mode, null, parent);
    }

    private List<String> buildCmd() {
        List<String> cmd = new ArrayList<>();
        cmd.add(DrivestrengthGuiMain.DRIVESTRENGTH_BIN.getAbsolutePath());

        this.addGeneralParamsTo(cmd);
        this.addAdvancedParamsTo(cmd);

        cmd.add(params.getTextValue(TextParam.NetlistFile));

        return cmd;
    }

    private void addGeneralParamsTo(List<String> cmd) {
        cmd.add("-o");
        cmd.add("3");

        cmd.add("-lib");
        cmd.add(params.getTextValue(TextParam.LibertyFile));

        cmd.add("-cellInfoJson");
        cmd.add(params.getTextValue(TextParam.cellInfoJsonFile));

        cmd.add("-out");
        cmd.add(params.getTextValue(GeneralTextParam.OutDir) + "/" + params.getTextValue(GeneralTextParam.OutFile));

        String remoteConfigPath = params.getTextValue(TextParam.RemoteConfigFile);
        if (!remoteConfigPath.isEmpty()) {
            cmd.add("-remoteConfig");
            cmd.add(remoteConfigPath);
        }
    }

    private void addAdvancedParamsTo(List<String> cmd) {
        cmd.add("-optimizeEnergyPercentage");
        cmd.add(Integer.toString(params.getIntValue(IntParam.optimizeEnergyPercentage)));
    }

}
