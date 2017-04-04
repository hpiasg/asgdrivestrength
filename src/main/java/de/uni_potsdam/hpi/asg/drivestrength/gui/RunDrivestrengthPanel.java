package de.uni_potsdam.hpi.asg.drivestrength.gui;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;

import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel;
import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractRunPanel;
import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractRunner.TerminalMode;
import de.uni_potsdam.hpi.asg.drivestrength.gui.DrivestrengthParameters.TextParam;

public class RunDrivestrengthPanel extends AbstractRunPanel {
    private static final long serialVersionUID = 6679558413574675095L;

    private DrivestrengthParameters params;
    private Window parent;
    private boolean userHitRun;

    public RunDrivestrengthPanel(Window parent, final DrivestrengthParameters params, boolean isDebug) {
        this(parent, params, isDebug, false, false);
    }

    public RunDrivestrengthPanel(final Window parent, final DrivestrengthParameters params,
                                   boolean isDebug, boolean hideGeneral, final boolean closeOnRun) {
        super(params);

        this.params = params;
        this.parent = parent;
        this.userHitRun = false;

        this.setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        this.add(tabbedPane, BorderLayout.CENTER);

        this.constructGeneralPanel(tabbedPane);
        //this.constructAdvancedPanel(tabbedPane);
        //this.constructDebugPanel(tabbedPane, isDebug);

        this.constructRunButton(closeOnRun);
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

        panel.addTextEntry(0, TextParam.NetlistFile, "Netlist file", "defaultNetlist.v",
                             true, JFileChooser.FILES_ONLY, false);

        addOutSection(panel, 2, "defaultOutfile.v", "defaultOutdir");
        getDataFromPanel(panel);
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
