package io.github.kosong.hyperusageprobe;

import io.github.kosong.hyperusageprobe.hook.HookTargets;
import io.github.kosong.hyperusageprobe.hook.MiSettingsProbe;
import io.github.kosong.hyperusageprobe.util.HookLog;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class XposedEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(
            XC_LoadPackage.LoadPackageParam loadPackageParam
    ) {
        if (!HookTargets.TARGET_PACKAGE.equals(
                loadPackageParam.packageName
        )) {
            return;
        }

        /*
         * 当前目标页面运行于默认进程。
         * 暂时不向未知的远程进程安装探针。
         */
        if (!HookTargets.TARGET_PACKAGE.equals(
                loadPackageParam.processName
        )) {
            HookLog.i(
                    "skip non-default process: "
                            + loadPackageParam.processName
            );
            return;
        }

        HookLog.i(
                "target loaded"
                        + ", package=" + loadPackageParam.packageName
                        + ", process=" + loadPackageParam.processName
                        + ", expectedVersion="
                        + HookTargets.TARGET_VERSION
        );

        MiSettingsProbe.install(loadPackageParam.classLoader);
    }
}