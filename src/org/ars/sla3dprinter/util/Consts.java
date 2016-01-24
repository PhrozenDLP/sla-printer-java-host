package org.ars.sla3dprinter.util;

import java.awt.*;

public class Consts {

    public static final String PREF_NODE_NAME = "org.ars.slaprinter";

    public static final String VERSION = "v0.4.5";
    public static final int VERSION_CODE = 405; // "va.b.c -> a * 10000 + b * 100 + c

    public static boolean sFLAG_DEBUG_MODE = false;

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
        PROJECTOR_OFF,
        PRINTER_PREFERENCE,
        PAUSE_PRINTING,
        RESUME_PRINTING
    }

    // Comm Bauds
    public static final String COMM_BAND_9600   = "9600";
    public static final String COMM_BAND_14400  = "14400";
    public static final String COMM_BAND_19200  = "19200";
    public static final String COMM_BAND_28800  = "28800";
    public static final String COMM_BAND_38400  = "38400";
    public static final String COMM_BAND_57600  = "57600";
    public static final String COMM_BAND_115200 = "115200";

    public static final int PULL_UP_STEPS = 4000;

    public static final String PATTERN_ESTIMATE_PROCESS_SUFFIX = "/%d, %dd %dh:%dm:%ds(total)";

    public static final int PROJECTOR_SWITCH_WAITING_TIME = 30 * 1000;

    public static final int DEBUG_TIME = 1 * 1000;

    public static final Font APP_FONT = new Font("Monaco", Font.PLAIN, 14);

    public static final int MAX_STEPS_PER_MOVE_COMMAND = 1000;

    public static final int MAX_EXPOSURE_MILLIS = 30 * 1000;
}
