package org.ars.sla3dprinter;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.ars.sla3dprinter.util.Consts;
import org.ars.sla3dprinter.util.PrefUtils;
import org.ars.sla3dprinter.util.Utils;
import org.ars.sla3dprinter.view.MainWindow;

public class SLA3DPrinter {

    public static void main(String[] args) {
        if (args.length == 1 && "--debug".equals(args[0])) {
            Consts.sFLAG_DEBUG_MODE = true;
        }
        System.out.println("Is debug mode? " + Consts.sFLAG_DEBUG_MODE);

        // take the menu bar off the jframe
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        // set the name of the application menu item
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DLA Printer");

        // set the look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Utils.log(ex);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainWindow window = new MainWindow();
                    window.show();
                } catch (Exception ex) {
                    Utils.log(ex);
                    ex.printStackTrace();
                }
            }
        });
    }

}
