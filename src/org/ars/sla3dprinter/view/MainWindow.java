package org.ars.sla3dprinter.view;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.ars.sla3dprinter.util.Utils;

public class MainWindow implements ActionListener {
    private static final int START_POS_X = 100;
    private static final int START_POS_Y = 100;
    private static final int WIDTH = 500;
    private static final int HEIGHT = 100;

    private static final String ACTION_HALF_TURN_CW     = "half_turn_cw";
    private static final String ACTION_HALF_TURN_CCW    = "half_turn_ccw";
    private static final String ACTION_OPEN_PORT        = "open_port";
    private static final String ACTION_CLOSE_PORT       = "close_port";
    private static final String ACTION_OPEN_PROJECT     = "open_project";

    private static final byte[] BYTE_HALF_TURN_CW   = "l".getBytes();
    private static final byte[] BYTE_HALF_TURN_CCW  = "r".getBytes();

    private JFrame mFrmSla3dPrinter;

    private Vector<String> mCommPorts = new Vector<String>();
    private JComboBox mComboPorts;

    private Vector<GraphicsDevice> mGraphicDevices = new Vector<GraphicsDevice>();

    private String mSelectedPort;
    private SerialPort mSerialPort;
    private JButton mBtnCwHalfTurn;
    private JButton mBtnCcwHalfTurn;
    private JButton mBtnPortOpen;
    private JButton mBtnPortClose;
    private JComboBox mComboVGA;
    private JLabel mLblProject;
    private JButton mBtnOpenProject;

    /**
     * Create the application.
     */
    public MainWindow() {
        loadCommPorts();
        loadGraphicDevices();
        initViews();
    }

    private void initWindowFrame() {
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
    }

    // Step motor pane
    private void initStepMotorPanel() {
        JPanel stepMotorPane = new JPanel();
        stepMotorPane.setForeground(Color.BLUE);
        stepMotorPane.setToolTipText("");
        stepMotorPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Step Motor control",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        stepMotorPane.setBounds(6, 104, 288, 65);
        mFrmSla3dPrinter.getContentPane().add(stepMotorPane);
        GridBagLayout gbl_stepMotorPane = new GridBagLayout();
        gbl_stepMotorPane.columnWidths = new int[]{138, 138, 0};
        gbl_stepMotorPane.rowHeights = new int[]{37, 0};
        gbl_stepMotorPane.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        gbl_stepMotorPane.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        stepMotorPane.setLayout(gbl_stepMotorPane);

        mBtnCwHalfTurn = new JButton("CW half turn");
        GridBagConstraints gbc_mBtnCwHalfTurn = new GridBagConstraints();
        gbc_mBtnCwHalfTurn.fill = GridBagConstraints.BOTH;
        gbc_mBtnCwHalfTurn.insets = new Insets(0, 0, 0, 5);
        gbc_mBtnCwHalfTurn.gridx = 0;
        gbc_mBtnCwHalfTurn.gridy = 0;
        stepMotorPane.add(mBtnCwHalfTurn, gbc_mBtnCwHalfTurn);
        mBtnCwHalfTurn.setActionCommand(ACTION_HALF_TURN_CW);
        mBtnCwHalfTurn.addActionListener(this);

        mBtnCwHalfTurn.setEnabled(false);

        mBtnCcwHalfTurn = new JButton("CCW half turn");
        GridBagConstraints gbc_mBtnCcwHalfTurn = new GridBagConstraints();
        gbc_mBtnCcwHalfTurn.fill = GridBagConstraints.BOTH;
        gbc_mBtnCcwHalfTurn.gridx = 1;
        gbc_mBtnCcwHalfTurn.gridy = 0;
        stepMotorPane.add(mBtnCcwHalfTurn, gbc_mBtnCcwHalfTurn);
        mBtnCcwHalfTurn.setActionCommand(ACTION_HALF_TURN_CCW);
        mBtnCcwHalfTurn.addActionListener(this);
        mBtnCcwHalfTurn.setEnabled(false);
    }

    // COM port pane
    private void initComPortPanel() {
        JPanel comPortPane = new JPanel();
        comPortPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Com Port",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        comPortPane.setBounds(306, 6, 288, 94);
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

        boolean hasPorts = mCommPorts.size() != 0;
        mBtnPortOpen.setEnabled(hasPorts);
        mComboPorts.setEnabled(hasPorts);
        mBtnPortClose.setEnabled(false);
    }

    // VGA Output section
    private void initVGAOutputPanel() {
        JPanel vgaOutputPane = new JPanel();
        vgaOutputPane.setBorder(new TitledBorder(new EtchedBorder(
                        EtchedBorder.LOWERED, null, null), "VGA Ouput",
                        TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        vgaOutputPane.setBounds(306, 104, 288, 65);
        mFrmSla3dPrinter.getContentPane().add(vgaOutputPane);
        GridBagLayout gbl_vgaOutputPane = new GridBagLayout();
        gbl_vgaOutputPane.columnWidths = new int[]{276, 0};
        gbl_vgaOutputPane.rowHeights = new int[]{27, 0};
        gbl_vgaOutputPane.columnWeights = new double[]{0.0, Double.MIN_VALUE};
        gbl_vgaOutputPane.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        vgaOutputPane.setLayout(gbl_vgaOutputPane);

        mComboVGA = new JComboBox(mGraphicDevices);
        mComboVGA.setSelectedIndex(0);
        GridBagConstraints gbc_mComboVGA = new GridBagConstraints();
        gbc_mComboVGA.anchor = GridBagConstraints.NORTH;
        gbc_mComboVGA.fill = GridBagConstraints.HORIZONTAL;
        gbc_mComboVGA.gridx = 0;
        gbc_mComboVGA.gridy = 0;
        vgaOutputPane.add(mComboVGA, gbc_mComboVGA);
    }

    // Init Input project panes
    private void initInputProjectPanel() {
        JPanel projectPane = new JPanel();
        projectPane.setForeground(Color.BLUE);
        projectPane.setBorder(new TitledBorder(new EtchedBorder(
                        EtchedBorder.LOWERED, null, null), "3D Model projejct",
                        TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        projectPane.setBounds(6, 6, 288, 94);
        mFrmSla3dPrinter.getContentPane().add(projectPane);
        projectPane.setLayout(null);

        mLblProject = new JLabel("");
        mLblProject.setHorizontalAlignment(SwingConstants.LEFT);
        mLblProject.setBounds(6, 22, 276, 28);
        mLblProject.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        projectPane.add(mLblProject);

        mBtnOpenProject = new JButton("Open Project");
        mBtnOpenProject.setBounds(144, 55, 138, 33);
        mBtnOpenProject.setActionCommand(ACTION_OPEN_PROJECT);
        mBtnOpenProject.addActionListener(this);
        projectPane.add(mBtnOpenProject);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initViews() {
        initWindowFrame();
        initStepMotorPanel();
        initComPortPanel();
        initVGAOutputPanel();
        initInputProjectPanel();
    }

    private void loadCommPorts() {
        String[] ports = SerialPortList.getPortNames();
        if (ports != null) {
            for (String port : ports) {
                mCommPorts.add(port);
            }
        }
    }

    private void loadGraphicDevices() {
        GraphicsDevice[] devices =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        if (devices != null) {
            for (GraphicsDevice device : devices) {
                mGraphicDevices.add(device);
            }
        }
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
            else if (ACTION_OPEN_PROJECT.equals(action)) {
                openFileChooser();
            }
            else {
                Utils.log("Unknown action: " + action);
            }
        } else {
            Utils.log("Unknown source: " + source);
        }
    }

    private void openFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open project");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // disable the "All files" option.
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            String path = null;
            try {
                path = selectedDir.getCanonicalPath();
            } catch (IOException e) {
                path = selectedDir.getName();
                e.printStackTrace();
            }
            mLblProject.setText(selectedDir.getName());
            mLblProject.setToolTipText(path);
        } else {
            System.out.println("No Selection ");
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
