package org.ars.sla3dprinter.util;

import java.awt.Window;
import java.lang.reflect.Method;

public class Utils {
    private static String OS = System.getProperty("os.name").toLowerCase();

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

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS
                .indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

    public static void enableFullScreenMode(Window window) {
        String className = "com.apple.eawt.FullScreenUtilities";
        String methodName = "setWindowCanFullScreen";

        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName, new Class<?>[] {
                    Window.class, boolean.class });
            method.invoke(null, window, true);
        } catch (Throwable t) {
            System.err.println("Full screen mode is not supported");
            t.printStackTrace();
        }
    }
}
