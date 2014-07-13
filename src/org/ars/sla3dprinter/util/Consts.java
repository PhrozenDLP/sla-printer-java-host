package org.ars.sla3dprinter.util;

public class Consts {

    // Actions
    public enum UIAction {
        COM_PORT_CHANGE,
        COM_BAUZ_CHANGE,
        VGA_PORT_CHANGE,
        OPEN_PORT,
        CLOSE_PORT,
        REFRESH_PORT,
        REFRESH_VGA,
        OPEN_PROJECT,
        START_PRINT,
        PLATFORM_UP,
        PLATFORM_DOWN,
        PROJECTOR_ON,
        PROJECTOR_OFF
    }

    // Comm Bauds
    public static final String COMM_BAND_9600   = "9600";
    public static final String COMM_BAND_14400  = "14400";
    public static final String COMM_BAND_19200  = "19200";
    public static final String COMM_BAND_28800  = "28800";
    public static final String COMM_BAND_38400  = "38400";
    public static final String COMM_BAND_57600  = "57600";
    public static final String COMM_BAND_115200 = "115200";
}
