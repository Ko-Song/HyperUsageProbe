package io.github.kosong.hyperusageprobe.util;

import android.app.ActivityThread;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import de.robv.android.xposed.XposedBridge;

import io.github.kosong.hyperusageprobe.log.LogContract;

public final class HookLog {

    public static final String PREFIX =
            "[HyperUsageProbe] ";

    private static final String AUTHORITY =
            LogContract.AUTHORITY;

    private static final Uri LOG_URI =
            Uri.parse(
                    "content://" + AUTHORITY
            );

    private HookLog() {
    }

    public static void i(
            String message
    ) {
        write(
                "I",
                message,
                null
        );
    }

    public static void e(
            String message,
            Throwable throwable
    ) {
        write(
                "E",
                message,
                throwable
        );
    }

    private static void write(
            String level,
            String message,
            Throwable throwable
    ) {
        String line =
                buildLine(
                        level,
                        message,
                        throwable
                );

        /*
         * 保留 LSPosed 日志作为兜底。
         * 后续生产版可以关闭普通 I 级别，只保留异常。
         */
        XposedBridge.log(line);

        try {
            Context context =
                    ActivityThread.currentApplication();

            if (context == null) {
                return;
            }

            ContentResolver resolver =
                    context.getContentResolver();

            Bundle extras =
                    new Bundle();

            extras.putString(
                    LogContract.KEY_LINE,
                    line
            );

            resolver.call(
                    LOG_URI,
                    LogContract.METHOD_WRITE,
                    "HyperUsageProbe",
                    extras
            );
        } catch (Throwable ignored) {
            /*
             * Provider 不可用时不能影响目标应用。
             */
        }
    }

    private static String buildLine(
            String level,
            String message,
            Throwable throwable
    ) {
        StringBuilder builder =
                new StringBuilder();

        builder.append(
                PREFIX
        );

        builder.append(level);
        builder.append(" [");
        builder.append(
                Thread.currentThread().getName()
        );
        builder.append("] ");
        builder.append(
                message == null
                        ? ""
                        : message
        );

        if (throwable != null) {
            builder.append(" | ");
            builder.append(
                    throwable.getClass().getName()
            );
            builder.append(": ");
            builder.append(
                    throwable.getMessage() == null
                            ? ""
                            : throwable.getMessage()
            );
        }

        return builder.toString();
    }
}
