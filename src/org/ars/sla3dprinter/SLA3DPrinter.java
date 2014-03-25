package org.ars.sla3dprinter;

import java.awt.EventQueue;

import org.ars.sla3dprinter.view.MainWindow;

public class SLA3DPrinter {

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainWindow window = new MainWindow();
                    window.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
