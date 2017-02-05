package de.uni_potsdam.hpi.asg.drivestrength.remotesimulation;

import de.uni_potsdam.hpi.asg.common.remote.RemoteInformation;
import de.uni_potsdam.hpi.asg.common.remote.SimpleRemoteOperationWorkflow;

public class SimulationRemoteOperationWorkflow extends SimpleRemoteOperationWorkflow {

    public SimulationRemoteOperationWorkflow(RemoteInformation rinfo, String subdir) {
        super(rinfo, subdir);
    }

    @Override
    protected boolean executeCallBack(String script, int code) {
        return true;
    }

}
