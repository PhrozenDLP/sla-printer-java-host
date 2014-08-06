package org.ars.sla3dprinter.util;

import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialUtils {

    public static SerialPort openPort(String portName) {
        try {
            SerialPort serialPort = new SerialPort(portName);
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

    public static  boolean closePort(SerialPort port) {
        if (port == null || !port.isOpened()) return true;
        try {
            return port.closePort();
        } catch (SerialPortException ex) {
            Utils.log(ex);
            return false;
        }
    }

    public static  boolean isPortAvailable(SerialPort port) {
        return port != null && port.isOpened();
    }

    public static void writeToPort(SerialPort port, String data) {
        if (port == null) return;
        if (TextUtils.isEmpty(data)) {
            System.err.println("No data to write");
            return;
        }
        System.out.println("Data to serial: " + data);
        if (port != null) {
            writeToPort(port, data.getBytes());
        }
    }

    private static void writeToPort(SerialPort port, byte[] data) {
        if (port == null) return;
        try {
            port.writeBytes(data);
        } catch (SerialPortException ex) {
            Utils.log(ex);
        }
    }
}
