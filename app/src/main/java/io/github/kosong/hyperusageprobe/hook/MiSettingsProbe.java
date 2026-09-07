package io.github.kosong.hyperusageprobe.hook;

import android.content.Context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import io.github.kosong.hyperusageprobe.util.HookLog;
import io.github.kosong.hyperusageprobe.util.ReflectUtils;

/**
 * 只记录、不修改的小米屏幕时间探针。
 */
public final class MiSettingsProbe {

    private static final AtomicBoolean INSTALLED =
            new AtomicBoolean(false);

    private MiSettingsProbe() {
    }

    public static void install(ClassLoader classLoader) {
        if (!INSTALLED.compareAndSet(false, true)) {
            HookLog.i("probe already installed");
            return;
        }

        HookLog.i("installing probe hooks");

        hookDeviceUsageDetails(classLoader);
        hookNameAndCategoryDetails(classLoader);
        hookUnlockUsageDetails(classLoader);
        hookCentralLoader(classLoader);

        HookLog.i("probe hook installation finished");
    }

    /**
     * 总屏幕使用时长及其图表转换。
     */
    private static void hookDeviceUsageDetails(
            ClassLoader classLoader
    ) {
        try {
            Class<?> deviceAppUsageClass =
                    XposedHelpers.findClass(
                            HookTargets.DEVICE_APP_USAGE,
                            classLoader
                    );

            XposedHelpers.findAndHookMethod(
                    HookTargets.DEVICE_APP_USAGE_KT,
                    classLoader,
                    "asDeviceUsageDetails",
                    deviceAppUsageClass,
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(
                                MethodHookParam param
                        ) {
                            try {
                                Object usage = param.args[0];
                                String dateType =
                                        String.valueOf(param.args[1]);

                                long totalDuration =
                                        ReflectUtils.callLong(
                                                usage,
                                                "getTotalDuration",
                                                -1L
                                        );

                                long lastCycle =
                                        ReflectUtils.callLong(
                                                usage,
                                                "getLastCycle",
                                                -1L
                                        );

                                Object detail =
                                        ReflectUtils.call(
                                                usage,
                                                "getDetail"
                                        );

                                HookLog.i(
                                        "device-before"
                                                + ", dateType=" + dateType
                                                + ", totalDuration="
                                                + totalDuration
                                                + ", lastCycle="
                                                + lastCycle
                                                + ", detailSize="
                                                + ReflectUtils.sizeOf(detail)
                                                + ", detail="
                                                + ReflectUtils.limitedList(
                                                        detail,
                                                        32
                                                )
                                );
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "device-before failed",
                                        throwable
                                );
                            }
                        }

                        @Override
                        protected void afterHookedMethod(
                                MethodHookParam param
                        ) {
                            try {
                                Object result = param.getResult();

                                HookLog.i(
                                        "device-after"
                                                + ", resultClass="
                                                + ReflectUtils.className(result)
                                                + ", result="
                                                + String.valueOf(result)
                                );
                            } catch (Throwable throwa