package org.ars.sla3dprinter.util;

public class TextUtils {

    public static final boolean isEmpty(String text) {
        return text == null || text.trim().length() == 0;
    }

    public static final boolean isNumeric(String text) {
        return text.matches("[0-9]+");
    }
}
