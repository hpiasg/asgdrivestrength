package de.uni_potsdam.hpi.asg.drivestrength.gui;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractRunner;

public class DrivestrengthRunner extends AbstractRunner {
    private static final Logger logger = LogManager.getLogger();

    private DrivestrengthParameters params;

    public DrivestrengthRunner(DrivestrengthParameters params) {
        super(params);
        this.params = params;
    }

    public void run(TerminalMode mode) {
        this.run(mode, null);
    }

    public void run(TerminalMode mode, Window parent) {
        if(!this.parametersAreValid()) {
            return;
        }
        List<String> cmd = buildCmd();
        logger.warn("TODO: run cmd");
        //exec(cmd, "ASGdrivestrength terminal", mode, null, parent);
    }

    private boolean parametersAreValid() {
        logger.warn("TODO: check parameter validity");
        return true;
    }

    private List<String> buildCmd() {
        List<String> cmd = new ArrayList<>();
        cmd.add("");

        logger.warn("TODO: build cmd");

        return cmd;
    }

}
