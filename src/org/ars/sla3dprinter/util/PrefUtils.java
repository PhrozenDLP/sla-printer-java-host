package org.ars.sla3dprinter.util;

import java.util.prefs.Preferences;

/**
 * Created by jimytc on 7/8/15.
 */
public class PrefUtils {

    private static Preferences sPrefs;

    private static final String PREF_BASELAYER_STEPS_FROM_TOP = "pref_baselayer_steps_from_top";
    private static final int PREF_BASELAYER_STEPS_FROM_TOP_DEFAULT = 120000;

    private static final String PREF_DELAY_AFTER_ACTION = "pref_delay_after_action";
    public static final int PREF_DELAY_AFTER_ACTION_DEFAULT_MILLIS = 500;

    public static Preferences getInstance() {
        if (sPrefs == null) {
            sPrefs = Preferences.userRoot().node(Consts.PREF_NODE_NAME);
        }
        return sPrefs;
    }

    public static void setBaseLayerStepsFromTop(int steps) {
        if (steps > 0) getInstance().putInt(PREF_BASELAYER_STEPS_FROM_TOP, steps);
    }

    public static int getBaseLayerStepsFromTop() {
        return getInstance().getInt(PREF_BASELAYER_STEPS_FROM_TOP, PREF_BASELAYER_STEPS_FROM_TOP_DEFAULT);
    }

    public static void setDelayAfterActionDefaultMillis(int millis) {
        if (millis > 0) getInstance().putInt(PREF_DELAY_AFTER_ACTION, millis);
    }

    public static int getDelayAfterActionDefaultMillis() {
        return getInstance().getInt(PREF_DELAY_AFTER_ACTION,
                PREF_DELAY_AFTER_ACTION_DEFAULT_MILLIS);
    }
}
