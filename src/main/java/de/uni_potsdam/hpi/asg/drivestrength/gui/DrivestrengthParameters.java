package de.uni_potsdam.hpi.asg.drivestrength.gui;

import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel.AbstractDoubleParam;
import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel.AbstractEnumParam;
import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel.AbstractIntParam;
import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel.AbstractTextParam;
import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractParameters;

public class DrivestrengthParameters extends AbstractParameters {
    public static final String[] OPTIMIZERS = {"SA", "NOP", "ESE", "NSE", "SFL", "TOP", "BOT", "EDM", "FO"};

    public enum TextParam implements AbstractTextParam {
        NetlistFile,
        LibertyFile, cellInfoJsonFile,
        RemoteConfigFile,
        OutputConstraintFile
    }

    public enum IntParam implements AbstractIntParam {
        optimizeEnergyPercentage
    }

    public enum DoubleParam implements AbstractDoubleParam {
        outputPinCapacitance, inputDrivenMaxCIn
    }

    public enum EnumParam implements AbstractEnumParam {
        optimizer
    }

    public DrivestrengthParameters() {
        super(".v");
    }

    @Override
    public String getEnumValue(AbstractEnumParam param) {
        int index = mainpanel.getEnumValue(param);
        if (param == EnumParam.optimizer) {
            return OPTIMIZERS[index];
        }
        return null;
    }
}
