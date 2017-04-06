package de.uni_potsdam.hpi.asg.drivestrength.gui;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractRunner;
import de.uni_potsdam.hpi.asg.drivestrength.DrivestrengthGuiMain;
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

        cmd.add(params.getTextValue(TextParam.NetlistFile));

        return cmd;
    }

    private void addGeneralParamsTo(List<String> cmd) {
        cmd.add("-o");
        cmd.add("3");

        cmd.add("-lib");
        cmd.add(params.getTextValue(TextParam.LibertyFile));

        cmd.add("-stage");
        cmd.add(params.getTextValue(TextParam.StageCountsFile));

        cmd.add("-defaultSizes");
        cmd.add(params.getTextValue(TextParam.DefaultSizesFile));

        cmd.add("-orderedSizes");
        cmd.add(params.getTextValue(TextParam.OrderedSizesFile));

        String remoteConfigPath = params.getTextValue(TextParam.RemoteConfigFile);
        if (!remoteConfigPath.isEmpty()) {
            cmd.add("-remoteConfig");
            cmd.add(remoteConfigPath);
        }
    }

}
