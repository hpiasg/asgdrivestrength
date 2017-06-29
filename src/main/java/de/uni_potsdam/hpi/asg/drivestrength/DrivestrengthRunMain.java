package de.uni_potsdam.hpi.asg.drivestrength;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import de.uni_potsdam.hpi.asg.common.gui.WatchForCloseWindowAdapter;
import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper.Mode;
import de.uni_potsdam.hpi.asg.common.misc.CommonConstants;
import de.uni_potsdam.hpi.asg.drivestrength.runner.DrivestrengthParameters;
import de.uni_potsdam.hpi.asg.drivestrength.runner.RunDrivestrengthPanel;

public class DrivestrengthRunMain {

    public static final File DRIVESTRENGTH_BIN = new File(CommonConstants.DEF_BIN_DIR_FILE,
                                                  "ASGdrivestrength" + CommonConstants.SCRIPT_FILE_EXTENSION);

    public static void main(String[] args) {
        System.out.println("Hello World from ASGdrivestrength runner GUI");
        int status = main2(args);
        System.exit(status);
    }

    public static int main2(String[] args) {
        boolean isDebug = extractIsDebug(args);
        String dataDir = extractDataDir(args);

        LoggerHelper.initLogger(3, null, false, Mode.cmdline);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
            return 1;
        }

        DrivestrengthParameters params = new DrivestrengthParameters();

        JFrame runframe = new JFrame("ASGdrivestrength runner");
        RunDrivestrengthPanel runpanel = new RunDrivestrengthPanel(runframe, params, isDebug, false, false, dataDir);
        if(runpanel.hasErrorOccured()) {
            return 1;
        }
        runframe.getContentPane().add(runpanel);
        WatchForCloseWindowAdapter closeAdapter = new WatchForCloseWindowAdapter();
        runframe.addWindowListener(closeAdapter);
        runframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        runframe.pack();
        runframe.setLocationRelativeTo(null); //center
        runframe.setVisible(true);

        while(!closeAdapter.isClosed()) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
            }
        }
        return 0;
    }

    private static String extractDataDir(String[] args) {
        boolean nextIsDataDir = false;
        for (String str : args) {
            if (nextIsDataDir) {
                return str;
            }
            if (str.equals("-dataDir")) {
                nextIsDataDir = true;
            }
        }
        return "";
    }

    private static boolean extractIsDebug(String[] args) {
        for (String str : args) {
            if (str.equals("-debug")) {
                return true;
            }
        }
        return false;
    }

}
