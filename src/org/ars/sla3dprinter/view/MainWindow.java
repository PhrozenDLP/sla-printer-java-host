package org.ars.sla3dprinter.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.ars.sla3dprinter.util.Utils;

public class MainWindow implements ActionListener {
    private static final int START_POS_X = 100;
    private static final int START_POS_Y = 100;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 60;

    private static final String ACTION_HALF_TURN_CW     = "half_turn_cw";
    private static final String ACTION_HALF_TURN_CCW    = "half_turn_ccw";

    private static final byte[] BYTE_HALF_TURN_CW   = "l".getBytes();
    private static final byte[] BYTE_HALF_TURN_CCW  = "r".getBytes();

    private JFrame frmSladPrinter;

    private String[] mCommPorts;
    private JComboBox mPortsComboBox;

    private String mSelectedPort;

    /**
     * Create the application.
     */
    public MainWindow() {
        loadCommPorts();
        initViews();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initViews() {
        frmSladPrinter = new JFrame();
        frmSladPrinter.setTitle("SLA 3D Printer");
        frmSladPrinter.setBounds(START_POS_X, START_POS_Y, START_POS_X + WIDTH, START_POS_Y + HEIGHT);
        frmSladPrinter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmSladPrinter.getContentPane().setLayout(null);
        frmSladPrinter.setResizable(false);

        JLabel lblStepMotor = new JLabel("Step Motor control");
        lblStepMotor.setFont(new Font("Monaco", Font.PLAIN, 16));
        lblStepMotor.setForeground(SystemColor.controlHighlight);
        lblStepMotor.setHorizontalAlignment(SwingConstants.CENTER);
        lblStepMotor.setBounds(30, 59, 240, 30);
        frmSladPrinter.getContentPane().add(lblStepMotor);

        JButton btnCwHalfTurn = new JButton("CW half turn");
        btnCwHalfTurn.setActionCommand(ACTION_HALF_TURN_CW);
        btnCwHalfTurn.setBounds(30, 101, 120, 30);
        btnCwHalfTurn.addActionListener(this);
        frmSladPrinter.getContentPane().add(btnCwHalfTurn);

        JButton btnCcwHalfTurn = new JButton("CCW half turn");
        btnCcwHalfTurn.setActionCommand(ACTION_HALF_TURN_CCW);
        btnCcwHalfTurn.setBounds(159, 101, 120, 30);
        btnCcwHalfTurn.addActionListener(this);
        frmSladPrinter.getContentPane().add(btnCcwHalfTurn);

        JLabel lbldPrinter = new JLabel("Port:");
        lbldPrinter.setHorizontalAlignment(SwingConstants.RIGHT);
        lbldPrinter.setForeground(Color.BLUE);
        lbldPrinter.setFont(new Font("Monaco", Font.PLAIN, 16));
        lbldPrinter.setBounds(6, 17, 50, 30);
        frmSladPrinter.getContentPane().add(lbldPrinter);

        mPortsComboBox = new JComboBox(mCommPorts);
        mPortsComboBox.setBounds(68, 17, 222, 30);
        mPortsComboBox.insertItemAt("", 0);
        mPortsComboBox.setSelectedIndex(0);
        mPortsComboBox.addActionListener(this);

        frmSladPrinter.getContentPane().add(mPortsComboBox);
    }

    private void loadCommPorts() {
        mCommPorts = SerialPortList.getPortNames();
    }

    public void show() {
        frmSladPrinter.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if (source instanceof JComboBox) {
            JComboBox comboBox = (JComboBox) source;
            String comPorts = comboBox.getSelectedItem().toString();
            mSelectedPort = Utils.isTextEmpty(comPorts) ? null : comPorts;
        } else if (source instanceof JButton) {
            if (Utils.isTextEmpty(mSelectedPort)) {
                Utils.log("No selected comm port");
                return;
            }

            SerialPort serialPort = new SerialPort(mSelectedPort);
            try {
                // Open serial port
                serialPort.openPort();
                // Set params. Also you can set params by this string:
                // serialPort.setParams(9600, 8, 1, 0);
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                final String action = ae.getActionCommand();
                if (ACTION_HALF_TURN_CW.equals(action)) {
                    Utils.log("Half turn Clockwise");
                    serialPort.writeBytes(BYTE_HALF_TURN_CW);
                }
                else if (ACTION_HALF_TURN_CCW.equals(action)) {
                    Utils.log("Half turn Counter-Clockwise");
                    serialPort.writeBytes(BYTE_HALF_TURN_CCW);
                }
                else {
                    Utils.log("Unknown action: " + action);
                }
            } catch (SerialPortException ex) {
                Utils.log(ex);
            } finally {
                if (serialPort.isOpened()) {
                    try {
                        serialPort.closePort();
                    } catch (SerialPortException ex) {
                        Utils.log(ex);
                    }
                }
            }
        } else {
            Utils.log("Unknown source: " + source);
        }
    }
}
