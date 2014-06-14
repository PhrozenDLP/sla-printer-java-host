package org.ars.sla3dprinter.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.ars.sla3dprinter.util.Consts;
import org.ars.sla3dprinter.util.Consts.UIAction;
import org.ars.sla3dprinter.util.Utils;

import com.kitfox.svg.app.beans.SVGPanel;

public class MainWindow implements ActionListener {
    private static final int START_POS_X    = 100;
    private static final int START_POS_Y    = 100;
    private static final int WIDTH          = 630;
    private static final int HEIGHT         = 360;

    private Vector<String> mCommPorts = new Vector<String>();
    private Vector<String> mCommBauds = new Vector<String>();
    private Vector<GraphicsDevice> mGraphicDevices = new Vector<GraphicsDevice>();
    private File mSelectedProject;

    private JFrame mFrmSla3dPrinter;

    private JPanel mStepMotorPane;

    // UI components for Serial ports
    private JPanel mComPortPane;
    private String mSelectedPort;
    private SerialPort mSerialPort;
    private JComboBox mComboPorts;
    private JButton mBtnPortOpen;
    private JButton mBtnPortClose;
    private JButton mBtnPortRefresh;
    private JComboBox mComboBauds;

    // UI components for VGA display
    private JPanel mVgaOutputPane;
    private JComboBox mComboVGA;
    private JButton mBtnVGARefresh;

    // UI components for printing config
    private JFormattedTextField mInputBaseExpo;
    private JFormattedTextField mInputMm2Steps;
    private JFormattedTextField mInputLayerHeight;
    private JFormattedTextField mInputLayerExpo;
    private JFormattedTextField mInputTankHDeg;
    private JFormattedTextField mInputTankRestAng;

    // UI components for target project
    private JPanel mProjectPane;
    private JLabel mLblProject;
    private JButton mBtnOpenProject;
    private JButton mBtnPrint;
    private JLabel mLblEstimated;

    // Resource part for images
    private Font mUIFont = new Font("Monaco", Font.PLAIN, 14);
    private Image mImgRefresh;
    private NumberFormatter mIntegerInputFormat = new NumberFormatter(
                    new DecimalFormat());

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

        mIntegerInputFormat.setAllowsInvalid(false);
        mIntegerInputFormat.setValueClass(Integer.class);

        // Comm Baud bands
        mCommBauds.add(Consts.COMM_BAND_9600);
        mCommBauds.add(Consts.COMM_BAND_14400);
        mCommBauds.add(Consts.COMM_BAND_19200);
        mCommBauds.add(Consts.COMM_BAND_28800);
        mCommBauds.add(Consts.COMM_BAND_38400);
        mCommBauds.add(Consts.COMM_BAND_57600);
        mCommBauds.add(Consts.COMM_BAND_115200);
    }

    private void disposeResources() {
        mImgRefresh = null;
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initViews() {
        initWindowFrame();
        initStepMotorPanel();
        initServoMotorPanel();
        initComPortPanel();
        initVGAOutputPanel();
        initInputProjectPanel();
        initMiscPanel();
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
        mFrmSla3dPrinter.setResizable(true);
    }

    // Step motor pane
    private void initStepMotorPanel() {
        mStepMotorPane = new JPanel();
        mStepMotorPane.setForeground(Color.BLUE);
        mStepMotorPane.setToolTipText("");
        mStepMotorPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Step Motor control",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        mStepMotorPane.setBounds(368, 6, 350, 150);
        mFrmSla3dPrinter.getContentPane().add(mStepMotorPane);
        mStepMotorPane.setLayout(null);

        JLabel lblLayerStep = new JLabel("Steps / mm:");
        lblLayerStep.setFont(mUIFont);
        lblLayerStep.setBounds(10, 20, 100, 30);
        mStepMotorPane.add(lblLayerStep);

        JLabel lblLayerHeight = new JLabel("Layer Height(mm):");
        lblLayerHeight.setFont(mUIFont);
        lblLayerHeight.setBounds(10, 60, 150, 30);
        mStepMotorPane.add(lblLayerHeight);

        JLabel lblLayerExposure = new JLabel("Exposure(sec):");
        lblLayerExposure.setFont(mUIFont);
        lblLayerExposure.setBounds(10, 100, 120, 30);
        mStepMotorPane.add(lblLayerExposure);

        mInputMm2Steps = new JFormattedTextField(mIntegerInputFormat);
        mInputMm2Steps.setFont(mUIFont);
        mInputMm2Steps.setColumns(10);
        mInputMm2Steps.setBounds(105, 20, 80, 30);
        mStepMotorPane.add(mInputMm2Steps);

        mInputLayerHeight = new JFormattedTextField(mIntegerInputFormat);
        mInputLayerHeight.setFont(mUIFont);
        mInputLayerHeight.setColumns(10);
        mInputLayerHeight.setBounds(155, 60, 80, 30);
        mStepMotorPane.add(mInputLayerHeight);

        mInputLayerExpo = new JFormattedTextField(mIntegerInputFormat);
        mInputLayerExpo.setFont(mUIFont);
        mInputLayerExpo.setColumns(10);
        mInputLayerExpo.setBounds(130, 100, 80, 30);
        mStepMotorPane.add(mInputLayerExpo);
    }

    private void initServoMotorPanel() {
        JPanel mServoMotorPane = new JPanel();
        mServoMotorPane.setLayout(null);
        mServoMotorPane.setToolTipText("");
        mServoMotorPane.setForeground(Color.BLUE);
        mServoMotorPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Servo Motor control",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        mServoMotorPane.setBounds(368, 168, 350, 150);
        mFrmSla3dPrinter.getContentPane().add(mServoMotorPane);

        JLabel lblHorizontalDeg = new JLabel("Horizontal deg:");
        lblHorizontalDeg.setFont(mUIFont);
        lblHorizontalDeg.setBounds(10, 20, 120, 30);
        mServoMotorPane.add(lblHorizontalDeg);

        JLabel lblTankResetAngle = new JLabel("Tank reset deg:");
        lblTankResetAngle.setFont(mUIFont);
        lblTankResetAngle.setBounds(10, 60, 120, 30);
        mServoMotorPane.add(lblTankResetAngle);

        mInputTankHDeg = new JFormattedTextField(mIntegerInputFormat);
        mInputTankHDeg.setFont(mUIFont);
        mInputTankHDeg.setColumns(10);
        mInputTankHDeg.setBounds(140, 20, 80, 30);
        mServoMotorPane.add(mInputTankHDeg);

        mInputTankRestAng = new JFormattedTextField(mIntegerInputFormat);
        mInputTankRestAng.setFont(mUIFont);
        mInputTankRestAng.setColumns(10);
        mInputTankRestAng.setBounds(140, 60, 80, 30);
        mServoMotorPane.add(mInputTankRestAng);
    }

    // COM port pane
    private void initComPortPanel() {
        mComPortPane = new JPanel();
        mComPortPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Printer conntection",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        mComPortPane.setBounds(6, 6, 350, 130);
        mFrmSla3dPrinter.getContentPane().add(mComPortPane);
        mComPortPane.setLayout(null);

        mComboPorts = new JComboBox(mCommPorts);
        mComboPorts.setBounds(5, 20, 290, 30);
        mComPortPane.add(mComboPorts);
        mComboPorts.insertItemAt("", 0);
        mComboPorts.setSelectedIndex(0);
        mComboPorts.setActionCommand(UIAction.COM_PORT_CHANGE.name());
        mComboPorts.addActionListener(this);

        mBtnPortRefresh = new JButton("R");
        mBtnPortRefresh.setBounds(300, 20, 30, 30);
        if (mImgRefresh != null) {
            ImageIcon icon = new ImageIcon(mImgRefresh.getScaledInstance(20,
                    20, Image.SCALE_SMOOTH));
            mBtnPortRefresh.setIcon(icon);
            mBtnPortRefresh.setText("");
        }
        mBtnPortRefresh.setActionCommand(UIAction.REFRESH_PORT.name());
        mBtnPortRefresh.addActionListener(this);
        mComPortPane.add(mBtnPortRefresh);

        mBtnPortOpen = new JButton("Open");
        mBtnPortOpen.setBounds(150, 90, 85, 30);
        mBtnPortOpen.setActionCommand(UIAction.OPEN_PORT.name());
        mBtnPortOpen.addActionListener(this);
        mComPortPane.add(mBtnPortOpen);

        mBtnPortClose = new JButton("Close");
        mBtnPortClose.setBounds(240, 90, 85, 30);
        mBtnPortClose.setActionCommand(UIAction.CLOSE_PORT.name());
        mBtnPortClose.addActionListener(this);
        mComPortPane.add(mBtnPortClose);

        boolean hasPorts = mCommPorts.size() != 0;
        mBtnPortOpen.setEnabled(hasPorts);
        mComboPorts.setEnabled(hasPorts);
        mBtnPortClose.setEnabled(false);

        mComboBauds = new JComboBox(mCommBauds);
        mComboBauds.setSelectedIndex(0);
        mComboBauds.setEnabled(true);
        mComboBauds.setBounds(90, 55, 100, 30);
        mComboBauds.setActionCommand(UIAction.COM_BAUZ_CHANGE.name());
        mComPortPane.add(mComboBauds);

        JLabel lblBaud = new JLabel("Baud(Hz):");
        lblBaud.setFont(mUIFont);
        lblBaud.setBounds(10, 55, 80, 30);
        mComPortPane.add(lblBaud);
    }

    // VGA Output section
    private void initVGAOutputPanel() {
        mVgaOutputPane = new JPanel();
        mVgaOutputPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Projector connection",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        mVgaOutputPane.setBounds(6, 150, 350, 60);
        mFrmSla3dPrinter.getContentPane().add(mVgaOutputPane);
        mVgaOutputPane.setLayout(null);

        mComboVGA = new JComboBox(mGraphicDevices);
        mComboVGA.setBounds(5, 20, 290, 30);
        mComboVGA.setSelectedIndex(0);
        mComboVGA.setActionCommand(UIAction.VGA_PORT_CHANGE.name());
        mVgaOutputPane.add(mComboVGA);

        mBtnVGARefresh = new JButton("R");
        mBtnVGARefresh.setBounds(300, 20, 30, 30);
        if (mImgRefresh != null) {
            ImageIcon icon = new ImageIcon(mImgRefresh.getScaledInstance(20,
                    20, Image.SCALE_SMOOTH));
            mBtnVGARefresh.setIcon(icon);
            mBtnVGARefresh.setText("");
        }
        mBtnVGARefresh.setActionCommand(UIAction.REFRESH_VGA.name());
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
        mProjectPane.setBounds(368, 330, 350, 100);
        mFrmSla3dPrinter.getContentPane().add(mProjectPane);
        mProjectPane.setLayout(null);

        mLblProject = new JLabel("");
        mLblProject.setHorizontalAlignment(SwingConstants.LEFT);
        mLblProject.setBounds(6, 22, 335, 30);
        mLblProject.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mProjectPane.add(mLblProject);

        mBtnOpenProject = new JButton("Open Project");
        mBtnOpenProject.setBounds(100, 60, 120, 30);
        mBtnOpenProject.setActionCommand(UIAction.OPEN_PROJECT.name());
        mBtnOpenProject.addActionListener(this);
        mProjectPane.add(mBtnOpenProject);

        mBtnPrint = new JButton("Print");
        mBtnPrint.setBounds(220, 60, 120, 30);
        mProjectPane.add(mBtnPrint);
        mBtnPrint.setActionCommand(UIAction.START_PRINT.name());
        mBtnPrint.addActionListener(this);

    }

    private void initMiscPanel() {
        JPanel mMiscPane = new JPanel();
        mMiscPane.setLayout(null);
        mMiscPane.setToolTipText("");
        mMiscPane.setForeground(Color.BLUE);
        mMiscPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Misc",
                TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
        mMiscPane.setBounds(6, 220, 350, 200);

        JLabel lblBaseExposure = new JLabel("Base Exposure (sec):");
        lblBaseExposure.setFont(mUIFont);
        lblBaseExposure.setBounds(10, 20, 160, 30);
        mMiscPane.add(lblBaseExposure);

        JLabel lblEstimate = new JLabel("Estimate time:");
        lblEstimate.setFont(mUIFont);
        lblEstimate.setBounds(10, 60, 120, 30);
        mMiscPane.add(lblEstimate);

        mLblEstimated = new JLabel("0s");
        mLblEstimated.setFont(mUIFont);
        mLblEstimated.setBounds(130, 60, 150, 30);
        mMiscPane.add(mLblEstimated);

        mFrmSla3dPrinter.getContentPane().add(mMiscPane);

        mInputBaseExpo = new JFormattedTextField(mIntegerInputFormat);
        mInputBaseExpo.setFont(mUIFont);
        mInputBaseExpo.setBounds(175, 20, 80, 30);
        mMiscPane.add(mInputBaseExpo);
        mInputBaseExpo.setColumns(10);
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
        mFrmSla3dPrinter.requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        final String action = ae.getActionCommand();
        final Object source = ae.getSource();
        if (Utils.isTextEmpty(action)) return;
        if (source == null) return;

        switch (UIAction.valueOf(action)) {
            case COM_PORT_CHANGE:
                JComboBox comboBox = (JComboBox) source;
                String comPorts = comboBox.getSelectedItem().toString();
                mSelectedPort = Utils.isTextEmpty(comPorts) ? null : comPorts;
                break;
            case COM_BAUZ_CHANGE:
                break;
            case VGA_PORT_CHANGE:
                break;
            case OPEN_PORT:
                if (Utils.isTextEmpty(mSelectedPort)) {
                    Utils.log("No selected comm port");
                    return;
                }
                mSerialPort = openPort(mSelectedPort);
                if (mSerialPort != null) {
                    mBtnPortOpen.setEnabled(false);
                    mComboPorts.setEnabled(false);
                    mBtnPortClose.setEnabled(true);
                }
                break;
            case CLOSE_PORT:
                if (closePort(mSerialPort)) {
                    mBtnPortOpen.setEnabled(true);
                    mComboPorts.setEnabled(true);
                    mBtnPortClose.setEnabled(false);
                    mSerialPort = null;
                }
                break;
            case REFRESH_PORT:
                loadCommPorts();
                break;
            case REFRESH_VGA:
                loadGraphicDevices();
                break;
            case OPEN_PROJECT:
                openFileChooser();
                break;
            case START_PRINT:
                promptFakeFrame();
                break;
            default:
                Utils.log("Unknown action: " + action);
                break;
        }
    }

    private void openFileChooser() {
        FileNameExtensionFilter svgFilter =
                new FileNameExtensionFilter("Scalable Vector Graphic (.svg)",
                        "svg");

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
            GraphicsConfiguration config = device.getDefaultConfiguration();
            final JFrame f = new JFrame(config);
            SVGPanel svgPanel = new SVGPanel();
            svgPanel.setSvgURI(mSelectedProject.toURI());
            svgPanel.setScaleToFit(true);
            svgPanel.setAntiAlias(true);
            f.getContentPane().add(svgPanel);
            f.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent event) {
                }

                @Override
                public void keyReleased(KeyEvent event) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE:
                            f.dispose();
                    }
                }

                @Override
                public void keyPressed(KeyEvent event) {
                }
            });
            f.dispose();
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            f.setVisible(true);

            if (Utils.isMac()) {
                Utils.enableFullScreenMode(f);
            }
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(mFrmSla3dPrinter, message, "Error",
                JOptionPane.ERROR_MESSAGE);
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
