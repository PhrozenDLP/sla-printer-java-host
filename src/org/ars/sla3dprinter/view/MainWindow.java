package org.ars.sla3dprinter.view;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.ars.sla3dprinter.util.Utils;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainWindow implements ActionListener {
    private static final int START_POS_X = 100;
    private static final int START_POS_Y = 100;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 90;

    private static final String ACTION_HALF_TURN_CW     = "half_turn_cw";
    private static final String ACTION_HALF_TURN_CCW    = "half_turn_ccw";
    private static final String ACTION_OPEN_PORT        = "open_port";
    private static final String ACTION_CLOSE_PORT       = "close_port";

    private static final byte[] BYTE_HALF_TURN_CW   = "l".getBytes();
    private static final byte[] BYTE_HALF_TURN_CCW  = "r".getBytes();

    private JFrame mFrmSla3dPrinter;

    private String[] mCommPorts;
    private JComboBox mComboPorts;

    private String mSelectedPort;
    private SerialPort mSerialPort;
    private JButton mBtnCwHalfTurn;
    private JButton mBtnCcwHalfTurn;
    private JButton mBtnPortOpen;
    private JButton mBtnPortClose;

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
        mFrmSla3dPrinter = new JFrame();
        mFrmSla3dPrinter.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                if (mSerialPort != null) {
                    closePort(mSerialPort);
                }
            }
        });
        mFrmSla3dPrinter.setTitle("SLA 3D Printer");
        mFrmSla3dPrinter.setBounds(START_POS_X, START_POS_Y, START_POS_X + WIDTH,
                START_POS_Y + HEIGHT);
        mFrmSla3dPrinter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mFrmSla3dPrinter.getContentPane().setLayout(null);
        mFrmSla3dPrinter.setResizable(false);

        // Step motor pane
        JPanel stepMotorPane = new JPanel();
        stepMotorPane.setForeground(Color.BLUE);
        stepMotorPane.setToolTipText("");
        stepMotorPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Step Motor control",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        stepMotorPane.setBounds(6, 104, 288, 58);
        mFrmSla3dPrinter.getContentPane().add(stepMotorPane);
        stepMotorPane.setLayout(new GridLayout(0, 2, 0, 0));

        mBtnCwHalfTurn = new JButton("CW half turn");
        stepMotorPane.add(mBtnCwHalfTurn);
        mBtnCwHalfTurn.setActionCommand(ACTION_HALF_TURN_CW);

        mBtnCcwHalfTurn = new JButton("CCW half turn");
        stepMotorPane.add(mBtnCcwHalfTurn);
        mBtnCcwHalfTurn.setActionCommand(ACTION_HALF_TURN_CCW);
        mBtnCcwHalfTurn.addActionListener(this);
        mBtnCwHalfTurn.addActionListener(this);

        // COM port pane
        JPanel comPortPane = new JPanel();
        comPortPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Com Port",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        comPortPane.setBounds(6, 6, 288, 94);
        mFrmSla3dPrinter.getContentPane().add(comPortPane);
        GridBagLayout gbl_comPortPane = new GridBagLayout();
        comPortPane.setLayout(gbl_comPortPane);

        mComboPorts = new JComboBox(mCommPorts);
        GridBagConstraints gbc_mPortsComboBox = new GridBagConstraints();
        gbc_mPortsComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_mPortsComboBox.gridx = 0;
        gbc_mPortsComboBox.gridy = 0;
        gbc_mPortsComboBox.gridwidth = 2;
        comPortPane.add(mComboPorts, gbc_mPortsComboBox);
        mComboPorts.insertItemAt("", 0);
        mComboPorts.setSelectedIndex(0);
        mComboPorts.addActionListener(this);

        mBtnPortOpen = new JButton("Open");
        GridBagConstraints gbc_btnOpen = new GridBagConstraints();
        gbc_btnOpen.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnOpen.gridx = 0;
        gbc_btnOpen.gridy = 1;
        gbc_btnOpen.weightx = 0.5;
        mBtnPortOpen.setActionCommand(ACTION_OPEN_PORT);
        mBtnPortOpen.addActionListener(this);
        comPortPane.add(mBtnPortOpen, gbc_btnOpen);

        mBtnPortClose = new JButton("Close");
        GridBagConstraints gbc_btnClose = new GridBagConstraints();
        gbc_btnClose.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnClose.gridx = 1;
        gbc_btnClose.gridy = 1;
        gbc_btnClose.weightx = 0.5;
        mBtnPortClose.setActionCommand(ACTION_CLOSE_PORT);
        mBtnPortClose.addActionListener(this);
        comPortPane.add(mBtnPortClose, gbc_btnClose);

        // init buttons state
        mBtnCwHalfTurn.setEnabled(false);
        mBtnCcwHalfTurn.setEnabled(false);
        mBtnPortClose.setEnabled(false);
        if (mCommPorts.length == 0) {
            mBtnPortOpen.setEnabled(false);
            mComboPorts.setEnabled(false);
        } else {
            mBtnPortOpen.setEnabled(true);
            mComboPorts.setEnabled(true);
        }
    }

    private void loadCommPorts() {
        mCommPorts = SerialPortList.getPortNames();
    }

    public void show() {
        mFrmSla3dPrinter.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if (source instanceof JComboBox) {
            JComboBox comboBox = (JComboBox) source;
            String comPorts = comboBox.getSelectedItem().toString();
            mSelectedPort = Utils.isTextEmpty(comPorts) ? null : comPorts;
        } else if (source instanceof JButton) {
            final String action = ae.getActionCommand();
            if (ACTION_OPEN_PORT.equals(action)) {
                if (Utils.isTextEmpty(mSelectedPort)) {
                    Utils.log("No selected comm port");
                    return;
                }
                mSerialPort = openPort(mSelectedPort);
                if (mSerialPort != null) {
                    mBtnPortOpen.setEnabled(false);
                    mComboPorts.setEnabled(false);
                    mBtnPortClose.setEnabled(true);
                    mBtnCwHalfTurn.setEnabled(true);
                    mBtnCcwHalfTurn.setEnabled(true);
                }
            }
            else if (ACTION_CLOSE_PORT.equals(action)) {
                if (closePort(mSerialPort)) {
                    mBtnPortOpen.setEnabled(true);
                    mComboPorts.setEnabled(true);
                    mBtnPortClose.setEnabled(false);
                    mBtnCwHalfTurn.setEnabled(false);
                    mBtnCcwHalfTurn.setEnabled(false);
                    mSerialPort = null;
                }
            }
            else if (ACTION_HALF_TURN_CW.equals(action)) {
                if (isPortAvailable(mSerialPort)) {
                    Utils.log("Half turn Clockwise");
                    writeToPort(mSerialPort, BYTE_HALF_TURN_CW);
                }
            }
            else if (ACTION_HALF_TURN_CCW.equals(action)) {
                if (isPortAvailable(mSerialPort)) {
                    Utils.log("Half turn Clockwise");
                    writeToPort(mSerialPort, BYTE_HALF_TURN_CCW);
                }
            }
            else {
                Utils.log("Unknown action: " + action);
            }
        } else {
            Utils.log("Unknown source: " + source);
        }
    }

    private SerialPort openPort(String portName) {
        try {
            SerialPort serialPort = new SerialPort(mSelectedPort);
            // Open serial port
            serialPort.openPort();
            // Set params. Also you can set params by this string:
            // serialPort.setParams(9600, 8, 1, 0);
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            return serialPort;
        } catch (SerialPortException ex) {
            Utils.log(ex);
            return null;
        }
    }

    private boolean closePort(SerialPort port) {
        if (port == null || !port.isOpened()) return true;
        try {
            return port.closePort();
        } catch (SerialPortException ex) {
            Utils.log(ex);
            return false;
        }
    }

    private void writeToPort(SerialPort port, byte[] data) {
        try {
            port.writeBytes(data);
        } catch (SerialPortException ex) {
            Utils.log(ex);
        }
    }

    private boolean isPortAvailable(SerialPort port) {
        return port != null && port.isOpened();
    }
}
