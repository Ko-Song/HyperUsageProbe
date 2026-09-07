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
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "device-after failed",
                                        throwable
                                );
                            }
                        }
                    }
            );

            HookLog.i(
                    "hooked DeviceAppUsageKt"
                            + "#asDeviceUsageDetails"
            );
        } catch (Throwable throwable) {
            HookLog.e(
                    "hookDeviceUsageDetails installation failed",
                    throwable
            );
        }
    }

    /**
     * App 使用列表及分类列表转换。
     */
    private static void hookNameAndCategoryDetails(
            ClassLoader classLoader
    ) {
        try {
            Class<?> deviceAppUsageClass =
                    XposedHelpers.findClass(
                            HookTargets.DEVICE_APP_USAGE,
                            classLoader
                    );

            Class<?> appTypeClass =
                    XposedHelpers.findClass(
                            "c9.a",
                            classLoader
                    );

            XposedHelpers.findAndHookMethod(
                    HookTargets.DEVICE_APP_USAGE_KT,
                    classLoader,
                    "asNameAndCategoryDetails",
                    deviceAppUsageClass,
                    appTypeClass,
                    Context.class,
                    Integer.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(
                                MethodHookParam param
                        ) {
                            try {
                                Object deviceUsage = param.args[0];
                                Object appType = param.args[1];
                                Object limit = param.args[3];

                                HookLog.i(
                                        "list-before"
                                                + ", appType="
                                                + String.valueOf(appType)
                                                + ", limit="
                                                + String.valueOf(limit)
                                );

                                Object mapObject =
                                        ReflectUtils.call(
                                                deviceUsage,
                                                "getAppDetail"
                                        );

                                if (!(mapObject instanceof Map)) {
                                    HookLog.i(
                                            "list-before"
                                                    + ", appDetail is "
                                                    + ReflectUtils.className(
                                                            mapObject
                                                    )
                                    );
                                    return;
                                }

                                Map<?, ?> appMap = (Map<?, ?>) mapObject;

                                HookLog.i(
                                        "list-before"
                                                + ", appDetailSize="
                                                + appMap.size()
                                );

                                Object targetUsage =
                                        appMap.get(
                                                HookTargets.PROBE_APP_PACKAGE
                                        );

                                if (targetUsage == null) {
                                    HookLog.i(
                                            "probe app not found: "
                                                    + HookTargets
                                                    .PROBE_APP_PACKAGE
                                    );
                                    return;
                                }

                                String packageName =
                                        ReflectUtils.callString(
                                                targetUsage,
                                                "getPkgName"
                                        );

                                long useTime =
                                        ReflectUtils.callLong(
                                                targetUsage,
                                                "getUseTime",
                                                -1L
                                        );

                                long lastCycle =
                                        ReflectUtils.callLong(
                                                targetUsage,
                                                "getLastCycle",
                                                -1L
                                        );

                                String categoryId =
                                        ReflectUtils.callString(
                                                targetUsage,
                                                "getCategoryId"
                                        );

                                String categoryType =
                                        ReflectUtils.callString(
                                                targetUsage,
                                                "getCategoryType"
                                        );

                                Object detail =
                                        ReflectUtils.call(
                                                targetUsage,
                                                "getDetail"
                                        );

                                HookLog.i(
                                        "probe-app"
                                                + ", pkg=" + packageName
                                                + ", useTime=" + useTime
                                                + ", lastCycle=" + lastCycle
                                                + ", categoryId="
                                                + categoryId
                                                + ", categoryType="
                                                + categoryType
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
                                        "list-before failed",
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

                                Object appDetails =
                                        ReflectUtils.call(
                                                result,
                                                "getAppDetails"
                                        );

                                Object categoryDetails =
                                        ReflectUtils.call(
                                                result,
                                                "getCategoryDetails"
                                        );

                                HookLog.i(
                                        "list-after"
                                                + ", resultClass="
                                                + ReflectUtils.className(result)
                                                + ", appDetailsSize="
                                                + ReflectUtils.sizeOf(
                                                        appDetails
                                                )
                                                + ", categoryDetailsSize="
                                                + ReflectUtils.sizeOf(
                                                        categoryDetails
                                                )
                                );
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "list-after failed",
                                        throwable
                                );
                            }
                        }
                    }
            );

            HookLog.i(
                    "hooked DeviceAppUsageKt"
                            + "#asNameAndCategoryDetails"
            );
        } catch (Throwable throwable) {
            HookLog.e(
                    "hookNameAndCategoryDetails installation failed",
                    throwable
            );
        }
    }

    /**
     * 解锁次数、首次解锁时间及解锁图表转换。
     */
    private static void hookUnlockUsageDetails(
            ClassLoader classLoader
    ) {
        try {
            Class<?> unlockUsageClass =
                    XposedHelpers.findClass(
                            HookTargets.UNLOCK_USAGE,
                            classLoader
                    );

            XposedHelpers.findAndHookMethod(
                    HookTargets.UNLOCK_USAGE_KT,
                    classLoader,
                    "asUnlockUsageDetails",
                    unlockUsageClass,
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

                                int unlockTimes =
                                        ReflectUtils.callInt(
                                                usage,
                                                "getUnlockTimes",
                                                -1
                                        );

                                int lastCycle =
                                        ReflectUtils.callInt(
                                                usage,
                                                "getLastCycle",
                                                -1
                                        );

                                long firstTime =
                                        ReflectUtils.callLong(
                                                usage,
                                                "getFirstTime",
                                                -1L
                                        );

                                Object unlocks =
                                        ReflectUtils.call(
                                                usage,
                                                "getUnlocks"
                                        );

                                HookLog.i(
                                        "unlock-before"
                                                + ", dateType=" + dateType
                                                + ", unlockTimes="
                                                + unlockTimes
                                                + ", firstTime="
                                                + firstTime
                                                + ", lastCycle="
                                                + lastCycle
                                                + ", unlocksSize="
                                                + ReflectUtils.sizeOf(unlocks)
                                                + ", unlocks="
                                                + ReflectUtils.limitedList(
                                                        unlocks,
                                                        40
                                                )
                                );
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "unlock-before failed",
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

                                int unlockTimes =
                                        ReflectUtils.callInt(
                                                result,
                                                "getUnlockTimes",
                                                -1
                                        );

                                int maxValue =
                                        ReflectUtils.callInt(
                                                result,
                                                "getMaxValue",
                                                -1
                                        );

                                int avgValue =
                                        ReflectUtils.callInt(
                                                result,
                                                "getAvgValue",
                                                -1
                                        );

                                int lastCycle =
                                        ReflectUtils.callInt(
                                                result,
                                                "getLastCycle",
                                                -1
                                        );

                                long firstTime =
                                        ReflectUtils.callLong(
                                                result,
                                                "getFirstTime",
                                                -1L
                                        );

                                Object unlocks =
                                        ReflectUtils.call(
                                                result,
                                                "getUnlocks"
                                        );

                                HookLog.i(
                                        "unlock-after"
                                                + ", unlockTimes="
                                                + unlockTimes
                                                + ", maxValue=" + maxValue
                                                + ", avgValue=" + avgValue
                                                + ", firstTime=" + firstTime
                                                + ", lastCycle=" + lastCycle
                                                + ", unlocksSize="
                                                + ReflectUtils.sizeOf(unlocks)
                                                + ", unlocks="
                                                + ReflectUtils.limitedList(
                                                        unlocks,
                                                        40
                                                )
                                );
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "unlock-after failed",
                                        throwable
                                );
                            }
                        }
                    }
            );

            HookLog.i(
                    "hooked UnlockUsageKt"
                            + "#asUnlockUsageDetails"
            );
        } catch (Throwable throwable) {
            HookLog.e(
                    "hookUnlockUsageDetails installation failed",
                    throwable
            );
        }
    }

    /**
     * 中央加载器探针。
     *
     * ma.i0 是当前目标版本中的协程内部类。它可能先返回
     * COROUTINE_SUSPENDED，因此只在结果确实是 ScreenTimeDetails
     * 时输出最终模型。
     */
    private static void hookCentralLoader(
            ClassLoader classLoader
    ) {
        try {
            Class<?> centralLoaderClass =
                    XposedHelpers.findClass(
                            HookTargets.CENTRAL_LOADER,
                            classLoader
                    );

            XposedHelpers.findAndHookMethod(
                    centralLoaderClass,
                    "invokeSuspend",
                    Object.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(
                                MethodHookParam param
                        ) {
                            try {
                                Object query =
                                        ReflectUtils.getField(
                                                param.thisObject,
                                                "g"
                                        );

                                if (query == null) {
                                    HookLog.i(
                                            "central-before: query is null"
                                    );
                                    return;
                                }

                                long startTime =
                                        ReflectUtils.callLong(
                                                query,
                                                "getStartTime",
                                                -1L
                                        );

                                long endTime =
                                        ReflectUtils.callLong(
                                                query,
                                                "getEndTime",
                                                -1L
                                        );

                                String dateType =
                                        ReflectUtils.callString(
                                                query,
                                                "getDateType"
                                        );

                                int userId =
                                        ReflectUtils.callInt(
                                                query,
                                                "getUserId",
                                                -1
                                        );

                                Object isHomeValue =
                                        ReflectUtils.call(
                                                query,
                                                "isHome"
                                        );

                                HookLog.i(
                                        "central-before"
                                                + ", startTime="
                                                + startTime
                                                + ", endTime="
                                                + endTime
                                                + ", dateType="
                                                + dateType
                                                + ", userId="
                                                + userId
                                                + ", isHome="
                                                + String.valueOf(isHomeValue)
                                );
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "central-before failed",
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
                                String resultClass =
                                        ReflectUtils.className(result);

                                HookLog.i(
                                        "central-after"
                                                + ", resultClass="
                                                + resultClass
                                );

                                if (!HookTargets.SCREEN_TIME_DETAILS.equals(
                                        resultClass
                                )) {
                                    /*
                                     * 很可能是 COROUTINE_SUSPENDED 或其他
                                     * 协程中间结果，不进行进一步处理。
                                     */
                                    return;
                                }

                                Object deviceUsage =
                                        ReflectUtils.call(
                                                result,
                                                "getDeviceUsage"
                                        );

                                Object unlockUsage =
                                        ReflectUtils.call(
                                                result,
                                                "getUnlockUsage"
                                        );

                                Object names =
                                        ReflectUtils.call(
                                                result,
                                                "getNameAndCategoryDetails"
                                        );

                                HookLog.i(
                                        "central-result"
                                                + ", deviceUsage="
                                                + String.valueOf(deviceUsage)
                                );

                                HookLog.i(
                                        "central-result"
                                                + ", unlockUsage="
                                                + String.valueOf(unlockUsage)
                                );

                                Object appDetails =
                                        ReflectUtils.call(
                                                names,
                                                "getAppDetails"
                                        );

                                Object categoryDetails =
                                        ReflectUtils.call(
                                                names,
                                                "getCategoryDetails"
                                        );

                                HookLog.i(
                                        "central-result"
                                                + ", appDetailsSize="
                                                + ReflectUtils.sizeOf(
                                                        appDetails
                                                )
                                                + ", categoryDetailsSize="
                                                + ReflectUtils.sizeOf(
                                                        categoryDetails
                                                )
                                );
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "central-after failed",
                                        throwable
                                );
                            }
                        }
                    }
            );

            HookLog.i(
                    "hooked ma.i0#invokeSuspend"
            );
        } catch (Throwable throwable) {
            /*
             * 中央类名称是当前版本特定的。
             * 即使它失效，前三个稳定业务转换探针仍可以工作。
             */
            HookLog.e(
                    "hookCentralLoader installation failed",
                    throwable
            );
        }
    }
}