package org.ars.sla3dprinter.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
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
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.security.AccessControlException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.NumberFormatter;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.ars.sla3dprinter.util.Consts;
import org.ars.sla3dprinter.util.Consts.UIAction;
import org.ars.sla3dprinter.util.Utils;

import com.kitfox.svg.Circle;
import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGRoot;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGIcon;
import com.kitfox.svg.xml.StyleAttribute;

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

    // FileChooser
    final JFileChooser mFileChooser;
    {
        JFileChooser fc = new JFileChooser();
        try
        {
        fc.setDialogTitle("Open project");
        fc.setFileFilter(new FileFilter() {
            final Matcher matchLevelFile = Pattern.compile(".*\\.svg[z]?").matcher("");
            public boolean accept(File file)
            {
                if (file.isDirectory()) return true;
                matchLevelFile.reset(file.getName());
                return matchLevelFile.matches();
            }

            public String getDescription() { return "SVG file (*.svg, *.svgz)"; }
        });
        } catch (AccessControlException ex) {
            //Do not create file chooser if webstart refuses permissions
        }
        mFileChooser = fc;
    }

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
        initPlatformMotorPanel();
        initTankMotorPanel();
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
                System.exit(0);
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
    private void initPlatformMotorPanel() {
        mStepMotorPane = new JPanel();
        mStepMotorPane.setForeground(Color.BLUE);
        mStepMotorPane.setToolTipText("");
        mStepMotorPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Platform motor control",
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
        mInputMm2Steps.setValue(20);
        mStepMotorPane.add(mInputMm2Steps);

        mInputLayerHeight = new JFormattedTextField(mIntegerInputFormat);
        mInputLayerHeight.setFont(mUIFont);
        mInputLayerHeight.setColumns(10);
        mInputLayerHeight.setBounds(155, 60, 80, 30);
        mInputLayerHeight.setValue(1);
        mStepMotorPane.add(mInputLayerHeight);

        mInputLayerExpo = new JFormattedTextField(mIntegerInputFormat);
        mInputLayerExpo.setFont(mUIFont);
        mInputLayerExpo.setColumns(10);
        mInputLayerExpo.setBounds(130, 100, 80, 30);
        mInputLayerExpo.setValue(30);
        mStepMotorPane.add(mInputLayerExpo);
    }

    private void initTankMotorPanel() {
        JPanel mServoMotorPane = new JPanel();
        mServoMotorPane.setLayout(null);
        mServoMotorPane.setToolTipText("");
        mServoMotorPane.setForeground(Color.BLUE);
        mServoMotorPane.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Tank motor control",
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
        mInputTankHDeg.setValue(0);
        mServoMotorPane.add(mInputTankHDeg);

        mInputTankRestAng = new JFormattedTextField(mIntegerInputFormat);
        mInputTankRestAng.setFont(mUIFont);
        mInputTankRestAng.setColumns(10);
        mInputTankRestAng.setBounds(140, 60, 80, 30);
        mInputTankRestAng.setValue(10);
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
        mInputBaseExpo.setValue(30);
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
                if (isPortAvailable(mSerialPort)) {
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
        if (mFileChooser == null) return;
        int retValue = mFileChooser.showOpenDialog(null);
        if (retValue == JFileChooser.APPROVE_OPTION) {
            mSelectedProject = mFileChooser.getSelectedFile();
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

    SVGUniverse mSVGUniverse = SVGCache.getSVGUniverse();
    SVGDiagram mSelectedSVGDiagram;
    SVGRoot mSelectedSVGRoot;

    private void promptFakeFrame() {
        if (mSelectedProject == null) {
            showErrorDialog("Choose a SVG by \'Open Project\'");
            return;
        }
        if (mSelectedPort == null) {
            showErrorDialog("Connect to printer before start printing");
            return;
        }

        // 1. Collect printing related data
        PrintingInfo info = new PrintingInfo();
        info.baseExpoTimeInSeconds = Integer.parseInt(mInputBaseExpo.getText());
        info.layerExpoTimeInSeconds = Integer.parseInt(mInputLayerExpo.getText());
        info.layerHeightInMms = Integer.parseInt(mInputLayerHeight.getText());
        info.stepsPerMm = Integer.parseInt(mInputMm2Steps.getText());
        info.tankHorizontalDeg = Integer.parseInt(mInputTankHDeg.getText());
        info.tankResetDeg = Integer.parseInt(mInputTankRestAng.getText());

        // 2. Prepare GraphicsDevice
        Object selected = mComboVGA.getSelectedItem();
        final GraphicsDevice device;
        final GraphicsConfiguration config;
        final JFrame f;
        if (selected instanceof GraphicsDevice) {
            device = (GraphicsDevice) selected;
            config = device.getDefaultConfiguration();
            f = new JFrame(config);
        } else {
            device = null;
            config = null;
            f = null;
        }

        if (device == null || config == null || f == null) {
            showErrorDialog("Printing projector not ready");
            return;
        }

        // 3. Prepare Worker
        final ProjectWorker worker;

        // Load target SVG file for the 3d model
        SVGUniverse universe = SVGCache.getSVGUniverse();
        SVGDiagram diagram = universe.getDiagram(mSelectedProject.toURI());
        if (diagram == null) {
            showErrorDialog("Cannot load project frile");
            return;
        }

        SVGRoot root = diagram.getRoot();
        if (root == null) {
            showErrorDialog("Project content has problem");
            return;
        }

        StyleAttribute width = root.getPresAbsolute("width");
        StyleAttribute height = root.getPresAbsolute("height");
        int targetWidth = device.getDisplayMode().getWidth();
        float scaleW = targetWidth / width.getFloatValue();
        int targetHeight = Math.round(height.getFloatValue() * scaleW);

        String attribScale = String.format("scale(%d)", Math.round(scaleW));
        final DynamicIconPanel myPanel = new DynamicIconPanel(targetWidth, targetHeight, attribScale);
        // Load target SVG file for the 3d model
        worker = new ProjectWorker(myPanel, root, mSerialPort);

        f.getContentPane().add(myPanel);
        f.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {}

            @Override
            public void keyReleased(KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        f.dispose();
                        if (worker != null) {
                            worker.cancel(true);
                        }
                        f.removeKeyListener(this);
                }
            }

            @Override
            public void keyPressed(KeyEvent event) {}
        });
        f.setUndecorated(true);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        f.pack();
        f.setVisible(true);

        if (Utils.isMac()) {
            Utils.enableFullScreenMode(f);
        }
        device.setFullScreenWindow(f);

        // Kick-off worker
        worker.execute();
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

            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
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

    private void writeToPort(SerialPort port, String data) {
        writeToPort(port, data.getBytes());
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

class ProjectWorker extends SwingWorker<Void, SVGElement>
    implements SerialPortEventListener{

    private DynamicIconPanel panel;
    private SVGRoot root;
    private SerialPort serialPort;

    // Dummy item for black screen
    private final SVGElement circle = new Circle();

    private final Object lock = new Object();

    public ProjectWorker(DynamicIconPanel _panel, SVGRoot _root, SerialPort _serial) {
        panel = _panel;
        root = _root;
        serialPort = _serial;
    }


    @Override
    protected Void doInBackground() throws Exception {
        serialPort.addEventListener(this);

        int total = root.getNumChildren();
        List<SVGElement> children = new ArrayList<SVGElement>();
        children = root.getChildren(children);
        SVGElement element = null;
        for (int i = 0; i < total; i++) {
            element = children.get(i);
            publish(element);
            synchronized (lock) {
                lock.wait();
            }
            publish(circle);

            synchronized (lock) {
                lock.wait();
            }
//            Thread.sleep(300);
        }
        return null;
    }

    @Override
    protected void process(List<SVGElement> trunks) {
        if (isCancelled()) {
            return;
        }
        System.out.println(trunks);
        System.out.println(trunks.size());
        if (trunks.size() != 1) {
            return;
        }
        SVGElement element = trunks.get(0);
        panel.replaceLayerSVG(element);
        System.out.println("Printing " + element.getId());
    }

    @Override
    protected void done() {
        panel = null;
        root = null;
        try {
            serialPort.removeEventListener();
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
        serialPort = null;
    }

    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR()) { //If data is available
            if (event.getEventValue() > 0) {
                try {
                    if (getState() == StateValue.STARTED) {
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                    System.out.println(serialPort.readString());
                } catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        } else if (event.isCTS()) { //If CTS line has changed state
            System.out.println((event.getEventValue() == 1) ? "CTS - ON" : "CTS - OFF");
        } else if (event.isDSR()) { //If DSR line has changed state
            System.out.println((event.getEventValue() == 1) ? "DSR - ON" : "DSR - OFF");
        } else {
            System.out.println(event.toString());
        }
    }
}

class DynamicIconPanel extends JPanel {
    public static final long serialVersionUID = 0;

    final SVGIcon icon;
    URI uri;

    SVGUniverse universe = SVGCache.getSVGUniverse();
    SVGDiagram diagram;
    SVGElement layerElement;

    public DynamicIconPanel(int width, int height, String scale)
    {
        StringReader reader = new StringReader(makeDynamicSVG(width, height, scale));
        uri = universe.loadSVG(reader, "myImage");
        diagram = universe.getDiagram(uri);
        icon = new SVGIcon();
        icon.setAntiAlias(true);
        icon.setSvgURI(uri);

        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(width, height));
    }

    public void paintComponent(Graphics g)
    {
        final int width = getWidth();
        final int height = getHeight();

        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);

        icon.paintIcon(this, g, 0, 0);
    }

    private String makeDynamicSVG(int width, int height, String scale)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("<svg width=\"" + width +"\" height=\"" + height + "\" transform=\"" + scale + "\"></svg>");

        pw.close();
        return sw.toString();
    }

    public void replaceLayerSVG(SVGElement element) {
        resetLayers();

        if (element == null) {
            return;
        }

        layerElement = element;

        try {
            SVGRoot root = diagram.getRoot();
            root.loaderAddChild(null, layerElement);

            // Update animation state or group and it's decendants so that it
            // reflects new animation values.
            // We could also call diagram.update(0.0) or
            // SVGCache.getSVGUniverse().update(). Note that calling
            // circle.update(0.0) won't display anything since even though it
            // will update the circle's state,
            // it won't update the parent group's state.
            universe.updateTime();
            repaint();
        } catch (SVGException e) {
            e.printStackTrace();
        }
    }

    public void resetLayers() {
        try {
            SVGRoot root = diagram.getRoot();
            root.removeChild(layerElement);

            universe.updateTime();
            repaint();
        } catch (SVGException e) {
            e.printStackTrace();
        }
    }
}

class PrintingInfo {
    int baseExpoTimeInSeconds;
    int layerExpoTimeInSeconds;
    int layerHeightInMms;
    int stepsPerMm;
    int tankHorizontalDeg;
    int tankResetDeg;
}

interface PrinterExecutable {
    public String getCommand();
}

class PlatformMovement implements PrinterExecutable {
    public static final int DIRECTION_UP    = 0;
    public static final int DIRECTION_DOWN  = 1;

    private static final String CODE_DIR_UP     = "2";
    private static final String CODE_DIR_DOWN   = "3";

    private static final String PLATFORM_MOVEMENT_PATTERN = "G%2d Z%d;";
    final int direction;
    final int steps;

    public PlatformMovement(int _dir, int _steps) {
        direction = _dir;
        steps = _steps;
    }

    @Override
    public String getCommand() {
        return String.format(PLATFORM_MOVEMENT_PATTERN, getDirectionCode(), steps);
    }

    private String getDirectionCode() {
        switch (direction) {
            case DIRECTION_DOWN:
                return CODE_DIR_DOWN;
            case DIRECTION_UP:
                return CODE_DIR_UP;
            default:
                throw new IllegalArgumentException("Invalid direction code: " + direction);
        }
    }
}

class PauseCommand implements PrinterExecutable {
    private static final String PAUSE_COMMAND_PATTERN = "G04 P%d;";
    final int pauseTimeInSeconds;

    public PauseCommand(int _time) {
        pauseTimeInSeconds = _time;
    }

    @Override
    public String getCommand() {
        return String.format(PAUSE_COMMAND_PATTERN, pauseTimeInSeconds);
    }
}

class TankMovement implements PrinterExecutable {
    public static final int DIRECTION_UP    = 0;
    public static final int DIRECTION_DOWN  = 1;

    private static final String CODE_DIR_UP     = "2";
    private static final String CODE_DIR_DOWN   = "3";

    private static final String TANK_COMMAND_PATTERN = "M%2d Z%d;";
    final int direction;
    final int steps;

    public TankMovement(int _dir, int _steps) {
        direction = _dir;
        steps = _steps;
    }

    @Override
    public String getCommand() {
        return String.format(TANK_COMMAND_PATTERN, getDirectionCode(), steps);
    }

    private String getDirectionCode() {
        switch (direction) {
            case DIRECTION_DOWN:
                return CODE_DIR_DOWN;
            case DIRECTION_UP:
                return CODE_DIR_UP;
            default:
                throw new IllegalArgumentException("Invalid direction code: " + direction);
        }
    }
}

class ProjectorCommand implements PrinterExecutable {
    private static final String PROJECTOR_ON_PATTERN    = "G50";
    private static final String PROJECTOR_OFF_PATTERN   = "G51";

    private final boolean makeOn;

    public ProjectorCommand(boolean _toOn) {
        makeOn = _toOn;
    }

    @Override
    public String getCommand() {
        return makeOn ? PROJECTOR_ON_PATTERN : PROJECTOR_OFF_PATTERN;
    }
}

/**
 * G02 Z(steps); => linear move up <br/>
 * G03 Z(steps); => linear move down <br/>
 * G04 P(seconds); => delay <br/>
 * G50; => Send power on command <br/>
 * G51; => Send power off command <br/>
 * M02 Z(steps); => rotate tank up <br/>
 * M03 Z(steps); => rotate tank down <br/>
 * M100; => this help message <br/>
 * @author jimytc
 */
class PrinterScriptFactory {
    public static final int PAUSE_TIME_DEFAULT = 1; // 1 second

    public static List<PrinterExecutable> generateCommandForResetPlatform() {
        ArrayList<PrinterExecutable> homeCommandsList = new ArrayList<PrinterExecutable>();
        for (int i = 0; i < 10; i++) {
            homeCommandsList.add(generatePlatformMovement(PlatformMovement.DIRECTION_UP, 4000));
            homeCommandsList.add(generatePauseCommand(PAUSE_TIME_DEFAULT));
        }
        return homeCommandsList;
    }

    public static PrinterExecutable generatePlatformMovement(int dir, int steps) {
        return new PlatformMovement(dir, steps);
    }

    public static PrinterExecutable generatePauseCommand(int time) {
        return new PauseCommand(time);
    }

    public static PrinterExecutable generateTankMovement(int dir, int steps) {
        return new TankMovement(dir, steps);
    }

    public static PrinterExecutable generateProjectorCommand(boolean toOn) {
        return new ProjectorCommand(toOn);
    }
}