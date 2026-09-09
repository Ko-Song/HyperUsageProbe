package io.github.kosong.hyperusageprobe.config;

import android.content.Context;
import android.content.SharedPreferences;

import de.robv.android.xposed.XSharedPreferences;

import io.github.kosong.hyperusageprobe.util.HookLog;

public final class ConfigReader {

    private static final String MODULE_PACKAGE =
            "io.github.kosong.hyperusageprobe";

    private static XSharedPreferences xPreferences;

    private ConfigReader() {
    }

    public static synchronized ModuleConfig readForHook() {
        try {
            if (xPreferences == null) {
                xPreferences =
                        new XSharedPreferences(
                                MODULE_PACKAGE,
                                ModuleConfig.PREF_NAME
                        );

                xPreferences.makeWorldReadable();
            }

            xPreferences.reload();

            return ModuleConfig.load(
                    xPreferences
            );
        } catch (Throwable throwable) {
            HookLog.e(
                    "read hook config failed; defaults used",
                    throwable
            );

            return ModuleConfig.defaults();
        }
    }

    public static ModuleConfig readForApp(
            Context context
    ) {
        if (context == null) {
            return ModuleConfig.defaults();
        }

        SharedPreferences preferences =
                context.getSharedPreferences(
                        ModuleConfig.PREF_NAME,
                        Context.MODE_PRIVATE
                );

        return ModuleConfig.load(
                preferences
        );
    }

    public static void saveForApp(
            Context context,
            ModuleConfig config
    ) {
        if (context == null || config == null) {
            return;
        }

        SharedPreferences preferences =
                context.getSharedPreferences(
                        ModuleConfig.PREF_NAME,
                        Context.MODE_PRIVATE
                );

        config.save(preferences);
    }
}
