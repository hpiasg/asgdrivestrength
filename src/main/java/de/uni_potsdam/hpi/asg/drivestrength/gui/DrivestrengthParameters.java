package de.uni_potsdam.hpi.asg.drivestrength.gui;

import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel.AbstractEnumParam;
import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel.AbstractIntParam;
import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel.AbstractTextParam;
import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractParameters;

public class DrivestrengthParameters extends AbstractParameters {

    public enum TextParam implements AbstractTextParam {
        /*general*/ NetlistFile,
        /*cells*/ LibertyFile, cellInfoJsonFile,
        /*remote*/ RemoteConfigFile,
    }

    public enum IntParam implements AbstractIntParam {
        optimizeEnergyPercentage
    }

    public DrivestrengthParameters() {
        super(".v");
    }

    @Override
    public String getEnumValue(AbstractEnumParam param) {
        return null;
    }
}
