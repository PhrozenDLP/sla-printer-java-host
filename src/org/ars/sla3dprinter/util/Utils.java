package org.ars.sla3dprinter.util;

public class Utils {

    public static boolean isTextEmpty(String text) {
        return text == null || "".equals(text);
    }

    public static boolean isTextAllSpace(String text) {
        return isTextEmpty(text) || isTextEmpty(text.trim());
    }

    public static void log(Throwable ex) {
        System.out.println(ex);
    }

    public static void log(String msg) {
        System.out.println(msg);
    }

    public static void log(String format, Object... objects) {
        System.out.println(String.format(format, objects));
    }
}
