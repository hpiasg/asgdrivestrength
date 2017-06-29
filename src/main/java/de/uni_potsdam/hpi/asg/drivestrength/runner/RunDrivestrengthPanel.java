package de.uni_potsdam.hpi.asg.drivestrength.runner;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;

import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel;
import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractRunPanel;
import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractRunner.TerminalMode;
import de.uni_potsdam.hpi.asg.drivestrength.runner.DrivestrengthParameters.DoubleParam;
import de.uni_potsdam.hpi.asg.drivestrength.runner.DrivestrengthParameters.EnumParam;
import de.uni_potsdam.hpi.asg.drivestrength.runner.DrivestrengthParameters.IntParam;
import de.uni_potsdam.hpi.asg.drivestrength.runner.DrivestrengthParameters.TextParam;

public class RunDrivestrengthPanel extends AbstractRunPanel {
    private static final long serialVersionUID = 6679558413574675095L;

    private DrivestrengthParameters params;
    private Window parent;
    private boolean userHitRun;
    private String dataDir;

    public RunDrivestrengthPanel(Window parent, final DrivestrengthParameters params, boolean isDebug) {
        this(parent, params, isDebug, false, false);
    }

    public RunDrivestrengthPanel(final Window parent, final DrivestrengthParameters params,
                                   boolean isDebug, boolean hideGeneral, final boolean closeOnRun) {
        this(parent, params, isDebug, hideGeneral, closeOnRun, "");
    }

    public RunDrivestrengthPanel(final Window parent, final DrivestrengthParameters params,
            boolean isDebug, boolean hideGeneral, final boolean closeOnRun, String dataDir) {
        super(params);

        this.params = params;
        this.parent = parent;
        this.userHitRun = false;
        this.dataDir = dataDir;

        this.setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        this.add(tabbedPane, BorderLayout.CENTER);

        this.constructGeneralPanel(tabbedPane);
        this.constructAdvancedPanel(tabbedPane);
        //this.constructDebugPanel(tabbedPane, isDebug);

        this.constructRunButton(closeOnRun);
    }

    private void constructAdvancedPanel(JTabbedPane tabbedPane) {
        PropertiesPanel panel = new PropertiesPanel(parent);
        tabbedPane.addTab("Advanced", null, panel, null);
        GridBagLayout gbl_advpanel = new GridBagLayout();
        gbl_advpanel.columnWidths = new int[]{200, 300, 30, 80, 0};
        gbl_advpanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_advpanel.rowHeights = new int[]{15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 0};
        gbl_advpanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_advpanel);

        panel.addComboBoxEntry(1, EnumParam.optimizer, "Optimizer", DrivestrengthParameters.OPTIMIZERS);

        panel.addSliderEntry(2, IntParam.optimizeEnergyPercentage, "Energy weight in SA-Optimizer", 0, 100, 0);

        panel.addSpinnerEntry(3, DoubleParam.outputPinCapacitance, "Output pin load capacitance (picofarad)", 0.01, 0.012);
        panel.addSpinnerEntry(4, DoubleParam.inputDrivenMaxCIn, "Capacitance limit for input-driven cells (picofarad)", 0.01, 0.005);

        getDataFromPanel(panel);
    }

    private void constructGeneralPanel(JTabbedPane tabbedPane) {
        PropertiesPanel panel = new PropertiesPanel(parent);
        tabbedPane.addTab("General", null, panel, null);
        GridBagLayout gbl_generalpanel = new GridBagLayout();
        gbl_generalpanel.columnWidths = new int[]{150, 300, 30, 80, 0};
        gbl_generalpanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_generalpanel.rowHeights = new int[]{15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 0};
        gbl_generalpanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_generalpanel);

        panel.addTextEntry(0, TextParam.NetlistFile, "Netlist file to optimize", normalizedPath("netlists/aNetlist.v"),
                true, JFileChooser.FILES_ONLY, false);

        panel.addTextEntry(2, TextParam.LibertyFile, "Liberty file", normalizedPath("cells/cellLibrary.lib"),
                true, JFileChooser.FILES_ONLY, false);

        panel.addTextEntry(3, TextParam.cellInfoJsonFile, "Cell Info JSON file", normalizedPath("cells/cellInfo.json"),
                true, JFileChooser.FILES_ONLY, false);

        panel.addTextEntry(4, TextParam.RemoteConfigFile, "Remote config file", normalizedPath("remoteConfig.json"),
                true, JFileChooser.FILES_ONLY, false);


        addOutSection(panel, 6, "aNetlist_optimized.v", normalizedPath("drivestrength-output"));

        panel.addTextEntry(8, TextParam.OutputConstraintFile, "Output constraints file (SDC)", "constraints.sdc");

        getDataFromPanel(panel);
    }

    private String normalizedPath(String path) {
        return Paths.get(dataDir + path).normalize().toString();
    }

    private void constructRunButton(boolean closeOnRun) {
        final RunDrivestrengthPanel thisRef = this;

        JButton runBtn = new JButton("Run");
        runBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(closeOnRun) {
                    parent.dispatchEvent(new WindowEvent(parent, WindowEvent.WINDOW_CLOSING));
                    thisRef.userHitRun = true;
                    return;
                }
                DrivestrengthRunner run = new DrivestrengthRunner(params);
                run.run(TerminalMode.frame);
            }
        });
        this.add(runBtn, BorderLayout.PAGE_END);
    }


    public boolean isUserHitRun() {
        return userHitRun;
    }


}
