package org.ars.sla3dprinter.view;

import org.ars.sla3dprinter.util.Consts;
import org.ars.sla3dprinter.util.PrefUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by jimytc on 7/8/15.
 */
public class PreferenceDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 2198962367349054754L;

    public static final String TITLE = "Printer Preference";

    private JTextField mInputBaseLayerStepsFromTop;
    private JTextField mInputDelayAfterAction;
    private JButton mBtnOk;

    public PreferenceDialog(Frame frame) {
        super(frame, TITLE, true);
        setModal(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        // Base layer from top
        JLabel lbBaseLayerStepsFromTop = new JLabel("BaseLayer steps from top:");
        lbBaseLayerStepsFromTop.setFont(Consts.APP_FONT);
        GridBagConstraints c1 = getGridBagConstraint(0, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        panel.add(lbBaseLayerStepsFromTop, c1);

        int steps = PrefUtils.getBaseLayerStepsFromTop();
        mInputBaseLayerStepsFromTop = new JTextField(Integer.toString(steps));
        mInputBaseLayerStepsFromTop.setFont(Consts.APP_FONT);
        mInputBaseLayerStepsFromTop.setSize(new Dimension(150, 30));
        GridBagConstraints c2 = getGridBagConstraint(1, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        panel.add(mInputBaseLayerStepsFromTop, c2);

        // Delay after action
        JLabel lbDelayAfterAction = new JLabel("Delay after action(ms):");
        lbBaseLayerStepsFromTop.setFont(Consts.APP_FONT);
        GridBagConstraints c3 = getGridBagConstraint(0, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        panel.add(lbDelayAfterAction, c3);

        int millis = PrefUtils.getDelayAfterActionDefaultMillis();
        mInputDelayAfterAction = new JTextField(Integer.toString(millis));
        mInputDelayAfterAction.setFont(Consts.APP_FONT);
        mInputDelayAfterAction.setSize(new Dimension(150, 30));
        GridBagConstraints c4 = getGridBagConstraint(1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        panel.add(mInputDelayAfterAction, c4);

        // Submit button
        mBtnOk = new JButton("OK");
        mBtnOk.setFont(Consts.APP_FONT);
        mBtnOk.setSize(new Dimension(50, 30));
        mBtnOk.addActionListener(this);
        GridBagConstraints c5 = getGridBagConstraint(1, 2, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        panel.add(mBtnOk, c5);

        getContentPane().add(panel);
        setResizable(false);
        pack();
        setLocationRelativeTo(frame);
    }

    public void actionPerformed(ActionEvent ev) {
        int steps = Integer.parseInt(mInputBaseLayerStepsFromTop.getText());
        PrefUtils.setBaseLayerStepsFromTop(steps);
        int millis = Integer.parseInt(mInputDelayAfterAction.getText());
        PrefUtils.setDelayAfterActionDefaultMillis(millis);
        setVisible(false);
    }

    private GridBagConstraints getGridBagConstraint(int gridx, int gridy, int gridwidth,
                                                    int gridheight, int fill, int anchor) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.fill = fill;
        constraints.anchor = anchor;
        return constraints;
    }
}