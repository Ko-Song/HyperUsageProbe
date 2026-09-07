package io.github.kosong.hyperusageprobe.util;

import de.robv.android.xposed.XposedBridge;

public final class HookLog {

    public static final String PREFIX = "[HyperUsageProbe] ";

    private HookLog() {
    }

    public static void i(String message) {
        XposedBridge.log(PREFIX + message);
    }

    public static void e(String message, Throwable throwable) {
        XposedBridge.log(PREFIX + "ERROR: " + message);

        if (throwable != null) {
            XposedBridge.log(throwable);
        }
    }
}