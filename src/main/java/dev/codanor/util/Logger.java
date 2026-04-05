package dev.codanor.util;

public class Logger {

    private static boolean _isLogging = false;

    public static void setLogging(boolean state) {
        _isLogging = state;
    }

    public static void log(String message) {
        if (!_isLogging) return;

        System.out.println(message);
    }
    public static void log(String tag, String message) {
        log("[" + tag + "] : " + message);
    }

    public static void warn(String message) {
        log("\u001B[33m" + message + "\u001B[0m");
    }
    public static void warn(String tag, String message) {
        warn("[" + tag + "] : " + message);
    }

}