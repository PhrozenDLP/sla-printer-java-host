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
    private JButton mBtnOk = new JButton("OK");

    public PreferenceDialog(Frame frame) {
        super(frame, TITLE, true);
        setModal(false);

        JLabel lbBaseLayerStepsFromTop = new JLabel("BaseLayer steps from top:");
        lbBaseLayerStepsFromTop.setFont(Consts.APP_FONT);

        int steps = PrefUtils.getBaseLayerStepsFromTop();
        mInputBaseLayerStepsFromTop = new JTextField(Integer.toString(steps));
        mInputBaseLayerStepsFromTop.setFont(Consts.APP_FONT);
        mInputBaseLayerStepsFromTop.setSize(new Dimension(150, 30));

        mBtnOk = new JButton("OK");
        mBtnOk.setFont(Consts.APP_FONT);
        mBtnOk.setSize(new Dimension(50, 30));
        mBtnOk.addActionListener(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 5));
        panel.add(lbBaseLayerStepsFromTop, BorderLayout.NORTH);
        panel.add(mInputBaseLayerStepsFromTop, BorderLayout.CENTER);
        panel.add(mBtnOk, BorderLayout.SOUTH);
        getContentPane().add(panel);

        setResizable(false);
        pack();
        setLocationRelativeTo(frame);
    }

    public void actionPerformed(ActionEvent ev) {
        int steps = Integer.parseInt(mInputBaseLayerStepsFromTop.getText());
        PrefUtils.setBaseLayerStepsFromTop(steps);
        setVisible(false);
    }
}