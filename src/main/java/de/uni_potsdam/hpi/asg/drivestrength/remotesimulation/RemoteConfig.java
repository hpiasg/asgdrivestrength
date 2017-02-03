package de.uni_potsdam.hpi.asg.drivestrength.remotesimulation;

import de.uni_potsdam.hpi.asg.common.remote.RemoteInformation;

public class RemoteConfig {

    private String host;
    private String username;
    private String password;
    private String remoteFolder;
    
    public RemoteConfig() {
    }
    
    public RemoteInformation asRemoteInformation() {
        return new RemoteInformation(host, username, password, remoteFolder);
    }

}
