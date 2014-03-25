package org.ars.sla3dprinter.view;

import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class MainWindow implements ActionListener {
    private static final int START_POS_X = 100;
    private static final int START_POS_Y = 100;
    private static final int WIDTH = 180;
    private static final int HEIGHT = 80;

    private static final String ACTION_HALF_TURN_CW = "half_turn_cw";
    private static final String ACTION_HALF_TURN_CCW = "half_turn_ccw";

    private JFrame frmSladPrinter;

    /**
     * Create the application.
     */
    public MainWindow() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmSladPrinter = new JFrame();
        frmSladPrinter.setTitle("SLA 3D Printer");
        frmSladPrinter.setBounds(START_POS_X, START_POS_Y, 260, 120);
        frmSladPrinter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmSladPrinter.getContentPane().setLayout(null);
        frmSladPrinter.setResizable(false);

        JLabel lblStepMotor = new JLabel("Step Motor control");
        lblStepMotor.setFont(new Font("Monaco", Font.PLAIN, 16));
        lblStepMotor.setForeground(SystemColor.controlHighlight);
        lblStepMotor.setHorizontalAlignment(SwingConstants.CENTER);
        lblStepMotor.setBounds(6, 6, 246, 29);
        frmSladPrinter.getContentPane().add(lblStepMotor);

        JButton btnCwHalfTurn = new JButton("CW half turn");
        btnCwHalfTurn.setActionCommand(ACTION_HALF_TURN_CW);
        btnCwHalfTurn.addActionListener(this);
        btnCwHalfTurn.setBounds(6, 48, 117, 29);
        frmSladPrinter.getContentPane().add(btnCwHalfTurn);

        JButton btnCcwHalfTurn = new JButton("CCW half turn");
        btnCcwHalfTurn.setActionCommand(ACTION_HALF_TURN_CCW);
        btnCcwHalfTurn.addActionListener(this);
        btnCcwHalfTurn.setBounds(135, 48, 117, 29);
        frmSladPrinter.getContentPane().add(btnCcwHalfTurn);

    }

    public void show() {
        frmSladPrinter.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        final String action = ae.getActionCommand();
        if (ACTION_HALF_TURN_CW.equals(action)) {
            System.out.println("Half turn Clockwise");
        }
        else if (ACTION_HALF_TURN_CCW.equals(action)) {
            System.out.println("Half turn Counter-Clockwise");
        }
        else {
            System.out.println("Unknown action: " + action);
        }
    }

}
