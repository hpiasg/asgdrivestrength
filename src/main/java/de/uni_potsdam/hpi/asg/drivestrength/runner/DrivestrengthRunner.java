package de.uni_potsdam.hpi.asg.drivestrength.runner;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractParameters.GeneralTextParam;
import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractRunner;
import de.uni_potsdam.hpi.asg.drivestrength.DrivestrengthRunMain;
import de.uni_potsdam.hpi.asg.drivestrength.runner.DrivestrengthParameters.DoubleParam;
import de.uni_potsdam.hpi.asg.drivestrength.runner.DrivestrengthParameters.EnumParam;
import de.uni_potsdam.hpi.asg.drivestrength.runner.DrivestrengthParameters.IntParam;
import de.uni_potsdam.hpi.asg.drivestrength.runner.DrivestrengthParameters.TextParam;

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
        cmd.add(DrivestrengthRunMain.DRIVESTRENGTH_BIN.getAbsolutePath());

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

        cmd.add("-outSdc");
        cmd.add(params.getTextValue(GeneralTextParam.OutDir) + "/" + params.getTextValue(TextParam.OutputConstraintFile));

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

        cmd.add("-outputPinCapacitance");
        cmd.add(Double.toString(params.getDoubleValue(DoubleParam.outputPinCapacitance)));

        cmd.add("-inputDrivenMaxCIn");
        cmd.add(Double.toString(params.getDoubleValue(DoubleParam.inputDrivenMaxCIn)));

        cmd.add("-optimizer");
        cmd.add(params.getEnumValue(EnumParam.optimizer).toString());
    }

}
