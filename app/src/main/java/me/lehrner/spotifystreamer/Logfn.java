package me.lehrner.spotifystreamer;

import android.util.Log;

@SuppressWarnings("WeakerAccess")
public final class Logfn {
    private static String getFnName() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[4];
        String methodName = e.getMethodName();
        String[] classNameParts = e.getClassName().split("\\.");
        String className = classNameParts[classNameParts.length-1];

        return className + "." + methodName + ":";
    }

    public static void d(String msg) {
        Log.d(getFnName(), msg);
    }

    public static void e(String msg) {
        Log.e(getFnName(), msg);
    }
}
