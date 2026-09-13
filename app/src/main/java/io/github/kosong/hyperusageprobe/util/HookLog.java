package io.github.kosong.hyperusageprobe.util;

import android.content.Context;
import android.os.Bundle;

import de.robv.android.xposed.XposedBridge;

import io.github.kosong.hyperusageprobe.log.LogContract;

public final class HookLog {

    public static final String PREFIX =
            "[HyperUsageProbe] ";

    private static final String AUTHORITY =
            LogContract.AUTHORITY;

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

        // 兜底写 LSPosed 日志
        XposedBridge.log(line);

        // 尝试写入模块私有日志
        try {
            Context context =
                    ActivityThread.currentApplication();

            if (context == null) {
                return;
            }

            Bundle extras =
                    new Bundle();

            extras.putString(
                    LogContract.KEY_LINE,
                    line
            );

            context.getContentResolver().call(
                    Uri.parse(
                            "content://" + AUTHORITY
                    ),
                    LogContract.METHOD_WRITE,
                    "HyperUsageProbe",
                    extras
            );
        } catch (Throwable ignored) {
            /*
             * 目标进程限制或 Provider 尚未启动时忽略，
             * 避免模块本身被杀死。
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

        builder.append(PREFIX);
        builder.append(level);
        builder.append(" [");
        builder.append(Thread.currentThread().getName());
        builder.append("] ");
        builder.append(message == null ? "" : message);

        if (throwable != null) {
            builder.append(" | ");
            builder.append(throwable.getClass().getName());
            builder.append(": ");
            builder.append(throwable.getMessage() == null
                    ? "" : throwable.getMessage());
        }

        return builder.toString();
    }
}