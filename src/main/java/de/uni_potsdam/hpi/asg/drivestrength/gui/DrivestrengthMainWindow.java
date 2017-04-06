package de.uni_potsdam.hpi.asg.drivestrength.gui;

import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.UIManager;

public class DrivestrengthMainWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    public DrivestrengthMainWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        setLayout(new GridBagLayout());
        setTitle("ASGDrivestrength");
        setSize(500, 300);

        JSlider percentageEnergySlider = new JSlider(JSlider.HORIZONTAL,
                0, 100, 0);

        JButton runButton = new JButton("Run");
        add(percentageEnergySlider);
        add(runButton);

        setVisible(true);
    }
}
