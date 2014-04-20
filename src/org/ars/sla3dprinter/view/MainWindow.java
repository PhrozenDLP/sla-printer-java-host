package org.ars.sla3dprinter.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.ars.sla3dprinter.util.Utils;
import org.eclipse.wb.swing.FocusTraversalOnArray;

import com.kitfox.svg.app.beans.SVGPanel;

public class MainWindow implements ActionListener {
    private static final int START_POS_X = 100;
    private static final int START_POS_Y = 100;
    private static final int WIDTH = 560;
    private static final int HEIGHT = 130;

    private static final String ACTION_HALF_TURN_CW = "half_turn_cw";
    private static final String ACTION_HALF_TURN_CCW = "half_turn_ccw";
    private static final String ACTION_OPEN_PORT = "open_port";
    private static final String ACTION_CLOSE_PORT = "close_port";
    private static final String ACTION_REFRESH_PORT = "refresh_port";
    private static final String ACTION_REFRESH_VGA = "refresh_vga";
    private static final String ACTION_OPEN_PROJECT = "open_project";
    private static final String ACTION_PRINT = "print";

    private static final byte[] BYTE_HALF_TURN_CW = "l".getBytes();
    private static final byte[] BYTE_HALF_TURN_CCW = "r".getBytes();

    private Vector<String> mCommPorts = new Vector<String>();
    private Vector<GraphicsDevice> mGraphicDevices = new Vector<GraphicsDevice>();
    private File mSelectedProject;

    private JFrame mFrmSla3dPrinter;

    private JPanel mStepMotorPane;
    private JButton mBtnCwHalfTurn;
    private JButton mBtnCcwHalfTurn;

    // UI components for Serial ports
    private JPanel mComPortPane;
    private String mSelectedPort;
    private SerialPort mSerialPort;
    private JComboBox mComboPorts;
    private JButton mBtnPortOpen;
    private JButton mBtnPortClose;
    private JButton mBtnPortRefresh;

    // UI components for VGA display
    private JPanel mVgaOutputPane;
    private JComboBox mComboVGA;
    private JButton mBtnVGARefresh;

    // UI components for target project
    private JPanel mProjectPane;
    private JLabel mLblProject;
    private JButton mBtnOpenProject;
    private JButton mBtnPrint;

    // Resource part for images
    private Image mImgRefresh;

    /**
     * Create the application.
     */
    public MainWindow() {
        prepareResources();
        loadCommPorts();
        loadGraphicDevices();
        initViews();
        disposeResources();
    }

    private void prepareResources() {
        try {
            mImgRefresh = ImageIO
                    .read(MainWindow.class
                            .getResource("/org/ars/sla3dprinter/images/ic_refresh.png"));
        } catch (IOException ex) {
            Utils.log(ex);
        }
    }

    private void disposeResources() {
        mImgRefresh = null;
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
        mFrmSla3dPrinter.setBounds(START_POS_X, START_POS_Y, START_POS_X
                + WIDTH, START_POS_Y + HEIGHT);
        mFrmSla3dPrinter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mFrmSla3dPrinter.getContentPane().setLayout(null);
        mFrmSla3dPrinter.setResizable(false);
    }

    // Step motor pane
    private void initStepMotorPanel() {
        mStepMotorPane = new JPanel();
        mStepMotorPane.setForeground(Color.BLUE);
        mStepMotorPane.setToolTipText("");
        mStepMotorPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Step Motor control",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        mStepMotorPane.setBounds(6, 104, 288, 65);
        mFrmSla3dPrinter.getContentPane().add(mStepMotorPane);
        mStepMotorPane.setLayout(null);

        mBtnCwHalfTurn = new JButton("CW half turn");
        mBtnCwHalfTurn.setBounds(6, 18, 133, 37);
        mStepMotorPane.add(mBtnCwHalfTurn);
        mBtnCwHalfTurn.setActionCommand(ACTION_HALF_TURN_CW);
        mBtnCwHalfTurn.addActionListener(this);

        mBtnCwHalfTurn.setEnabled(false);

        mBtnCcwHalfTurn = new JButton("CCW half turn");
        mBtnCcwHalfTurn.setBounds(144, 18, 138, 37);
        mStepMotorPane.add(mBtnCcwHalfTurn);
        mBtnCcwHalfTurn.setActionCommand(ACTION_HALF_TURN_CCW);
        mBtnCcwHalfTurn.addActionListener(this);
        mBtnCcwHalfTurn.setEnabled(false);
    }

    // COM port pane
    private void initComPortPanel() {
        mComPortPane = new JPanel();
        mComPortPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Com Port",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        mComPortPane.setBounds(306, 6, 350, 100);
        mFrmSla3dPrinter.getContentPane().add(mComPortPane);
        mComPortPane.setLayout(null);

        mComboPorts = new JComboBox(mCommPorts);
        mComboPorts.setBounds(5, 20, 290, 30);
        mComPortPane.add(mComboPorts);
        mComboPorts.insertItemAt("", 0);
        mComboPorts.setSelectedIndex(0);
        mComboPorts.addActionListener(this);

        mBtnPortRefresh = new JButton("R");
        mBtnPortRefresh.setBounds(300, 20, 30, 30);
        if (mImgRefresh != null) {
            ImageIcon icon = new ImageIcon(mImgRefresh.getScaledInstance(20,
                    20, Image.SCALE_SMOOTH));
            mBtnPortRefresh.setIcon(icon);
            mBtnPortRefresh.setText("");
        }
        mBtnPortRefresh.setActionCommand(ACTION_REFRESH_PORT);
        mBtnPortRefresh.addActionListener(this);
        mComPortPane.add(mBtnPortRefresh);

        mBtnPortOpen = new JButton("Open");
        mBtnPortOpen.setBounds(129, 53, 85, 30);
        mBtnPortOpen.setActionCommand(ACTION_OPEN_PORT);
        mBtnPortOpen.addActionListener(this);
        mComPortPane.add(mBtnPortOpen);

        mBtnPortClose = new JButton("Close");
        mBtnPortClose.setBounds(210, 53, 85, 30);
        mBtnPortClose.setActionCommand(ACTION_CLOSE_PORT);
        mBtnPortClose.addActionListener(this);
        mComPortPane.add(mBtnPortClose);

        boolean hasPorts = mCommPorts.size() != 0;
        mBtnPortOpen.setEnabled(hasPorts);
        mComboPorts.setEnabled(hasPorts);
        mBtnPortClose.setEnabled(false);
    }

    // VGA Output section
    private void initVGAOutputPanel() {
        mVgaOutputPane = new JPanel();
        mVgaOutputPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "VGA Ouput",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        mVgaOutputPane.setBounds(306, 104, 350, 60);
        mFrmSla3dPrinter.getContentPane().add(mVgaOutputPane);
        mVgaOutputPane.setLayout(null);

        mComboVGA = new JComboBox(mGraphicDevices);
        mComboVGA.setBounds(5, 20, 290, 30);
        mComboVGA.setSelectedIndex(0);
        mVgaOutputPane.add(mComboVGA);

        mBtnVGARefresh = new JButton("R");
        mBtnVGARefresh.setBounds(300, 20, 30, 30);
        if (mImgRefresh != null) {
            ImageIcon icon = new ImageIcon(mImgRefresh.getScaledInstance(20,
                    20, Image.SCALE_SMOOTH));
            mBtnVGARefresh.setIcon(icon);
            mBtnVGARefresh.setText("");
        }
        mBtnVGARefresh.setActionCommand(ACTION_REFRESH_VGA);
        mBtnVGARefresh.addActionListener(this);
        mVgaOutputPane.add(mBtnVGARefresh);
    }

    // Init Input project panes
    private void initInputProjectPanel() {
        mProjectPane = new JPanel();
        mProjectPane.setForeground(Color.BLUE);
        mProjectPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "3D Model projejct",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        mProjectPane.setBounds(6, 6, 288, 94);
        mFrmSla3dPrinter.getContentPane().add(mProjectPane);
        mProjectPane.setLayout(null);

        mLblProject = new JLabel("");
        mLblProject.setHorizontalAlignment(SwingConstants.LEFT);
        mLblProject.setBounds(6, 22, 276, 28);
        mLblProject.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mProjectPane.add(mLblProject);

        mBtnOpenProject = new JButton("Open Project");
        mBtnOpenProject.setBounds(144, 55, 138, 33);
        mBtnOpenProject.setActionCommand(ACTION_OPEN_PROJECT);
        mBtnOpenProject.addActionListener(this);
        mProjectPane.add(mBtnOpenProject);

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

        mBtnPrint = new JButton("Print");
        mBtnPrint.setActionCommand(ACTION_PRINT);
        mBtnPrint.addActionListener(this);
        mBtnPrint.setBounds(175, 173, 117, 29);
        mFrmSla3dPrinter.getContentPane().add(mBtnPrint);
        mFrmSla3dPrinter.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{mBtnPrint, mFrmSla3dPrinter.getContentPane(), mStepMotorPane, mBtnCwHalfTurn, mBtnCcwHalfTurn, mComPortPane, mComboPorts, mBtnPortRefresh, mBtnPortOpen, mBtnPortClose, mVgaOutputPane, mComboVGA, mBtnVGARefresh, mProjectPane, mLblProject, mBtnOpenProject}));
    }

    private void loadCommPorts() {
        mCommPorts.clear();
        String[] ports = SerialPortList.getPortNames();
        if (ports != null) {
            for (String port : ports) {
                mCommPorts.add(port);
            }
        }
    }

    private void loadGraphicDevices() {
        mGraphicDevices.clear();
        GraphicsDevice[] devices = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getScreenDevices();
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
            if (ACTION_REFRESH_PORT.equals(action)) {
                loadCommPorts();
            } else if (ACTION_REFRESH_VGA.equals(action)) {
                loadGraphicDevices();
            } else if (ACTION_OPEN_PORT.equals(action)) {
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
            } else if (ACTION_CLOSE_PORT.equals(action)) {
                if (closePort(mSerialPort)) {
                    mBtnPortOpen.setEnabled(true);
                    mComboPorts.setEnabled(true);
                    mBtnPortClose.setEnabled(false);
                    mBtnCwHalfTurn.setEnabled(false);
                    mBtnCcwHalfTurn.setEnabled(false);
                    mSerialPort = null;
                }
            } else if (ACTION_HALF_TURN_CW.equals(action)) {
                if (isPortAvailable(mSerialPort)) {
                    Utils.log("Half turn Clockwise");
                    writeToPort(mSerialPort, BYTE_HALF_TURN_CW);
                }
            } else if (ACTION_HALF_TURN_CCW.equals(action)) {
                if (isPortAvailable(mSerialPort)) {
                    Utils.log("Half turn Clockwise");
                    writeToPort(mSerialPort, BYTE_HALF_TURN_CCW);
                }
            } else if (ACTION_OPEN_PROJECT.equals(action)) {
                openFileChooser();
            } else if (ACTION_PRINT.equals(action)) {
                promptFakeFrame();
            } else {
                Utils.log("Unknown action: " + action);
            }
        } else {
            Utils.log("Unknown source: " + source);
        }
    }

    private void openFileChooser() {
        FileNameExtensionFilter svgFilter =
                new FileNameExtensionFilter("Scalable Vector Graphic (.svg)", "svg");

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open project");
        chooser.addChoosableFileFilter(svgFilter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int retValue = chooser.showOpenDialog(null);
        if (retValue == JFileChooser.APPROVE_OPTION) {
            mSelectedProject = chooser.getSelectedFile();
            if (mSelectedProject == null) {
                return;
            }

            String path = null;
            try {
                path = mSelectedProject.getCanonicalPath();
            } catch (IOException ex) {
                Utils.log(ex);
                path = mSelectedProject.getName();
            }
            mLblProject.setText(mSelectedProject.getName());
            mLblProject.setToolTipText(path);
        } else {
            System.out.println("No Selection ");
        }
    }

    private void promptFakeFrame() {
        if (mSelectedProject == null) {
            showErrorDialog("Choose a SVG by \'Open Project\'");
            return;
        }

        Object selected = mComboVGA.getSelectedItem();
        if (selected instanceof GraphicsDevice) {
            GraphicsDevice device = (GraphicsDevice) selected;
            JFrame f = new JFrame(device.getDefaultConfiguration());
            SVGPanel svgPanel = new SVGPanel();
            svgPanel.setSvgURI(mSelectedProject.toURI());
            svgPanel.setScaleToFit(true);
            f.setSize(600, 600);
            f.getContentPane().add(svgPanel);
            f.setLocation(100, 10);
            f.setVisible(true);
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(mFrmSla3dPrinter, message, "Error", JOptionPane.ERROR_MESSAGE);
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
