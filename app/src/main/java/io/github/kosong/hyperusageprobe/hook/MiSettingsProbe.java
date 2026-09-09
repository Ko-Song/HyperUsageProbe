package io.github.kosong.hyperusageprobe.hook;

import android.content.Context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import io.github.kosong.hyperusageprobe.util.HookLog;
import io.github.kosong.hyperusageprobe.util.ReflectUtils;

public final class MiSettingsProbe {

    private static final AtomicBoolean INSTALLED =
            new AtomicBoolean(false);

    private MiSettingsProbe() {
    }

    public static void install(
            ClassLoader classLoader
    ) {
        if (!INSTALLED.compareAndSet(
                false,
                true
        )) {
            HookLog.i(
                    "hooks already installed"
            );
            return;
        }

        HookLog.i(
                "installing hooks"
        );

        hookDeviceUsageDetails(
                classLoader
        );

        hookNameAndCategoryDetails(
                classLoader
        );

        hookUnlockUsageDetails(
                classLoader
        );

        hookCentralLoader(
                classLoader
        );

        HookLog.i(
                "hook installation finished"
        );
    }

    private static void hookDeviceUsageDetails(
            ClassLoader classLoader
    ) {
        try {
            Class<?> usageClass =
                    XposedHelpers.findClass(
                            HookTargets.DEVICE_APP_USAGE,
                            classLoader
                    );

            XposedHelpers.findAndHookMethod(
                    HookTargets.DEVICE_APP_USAGE_KT,
                    classLoader,
                    "asDeviceUsageDetails",
                    usageClass,
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(
                                MethodHookParam param
                        ) {
                            try {
                                Object usage =
                                        param.args[0];

                                String dateType =
                                        String.valueOf(
                                                param.args[1]
                                        );

                                HookLog.i(
                                        "device"
                                                + ", dateType="
                                                + dateType
                                                + ", total="
                                                + ReflectUtils.callLong(
                                                usage,
                                                "getTotalDuration",
                                                -1L
                                        )
                                                + ", detailSize="
                                                + ReflectUtils.sizeOf(
                                                ReflectUtils.call(
                                                        usage,
                                                        "getDetail"
                                                )
                                        )
                                );
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "device probe failed",
                                        throwable
                                );
                            }
                        }
                    }
            );

            HookLog.i(
                    "hooked device conversion"
            );
        } catch (Throwable throwable) {
            HookLog.e(
                    "device hook install failed",
                    throwable
            );
        }
    }

    private static void hookNameAndCategoryDetails(
            ClassLoader classLoader
    ) {
        try {
            Class<?> usageClass =
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
                    usageClass,
                    appTypeClass,
                    Context.class,
                    Integer.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(
                                MethodHookParam param
                        ) {
                            try {
                                Object result =
                                        param.getResult();

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
                                        "list"
                                                + ", appDetails="
                                                + ReflectUtils.sizeOf(
                                                appDetails
                                        )
                                                + ", categoryDetails="
                                                + ReflectUtils.sizeOf(
                                                categoryDetails
                                        )
                                );
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "list probe failed",
                                        throwable
                                );
                            }
                        }
                    }
            );

            HookLog.i(
                    "hooked list conversion"
            );
        } catch (Throwable throwable) {
            HookLog.e(
                    "list hook install failed",
                    throwable
            );
        }
    }

    private static void hookUnlockUsageDetails(
            ClassLoader classLoader
    ) {
        try {
            Class<?> usageClass =
                    XposedHelpers.findClass(
                            HookTargets.UNLOCK_USAGE,
                            classLoader
                    );

            XposedHelpers.findAndHookMethod(
                    HookTargets.UNLOCK_USAGE_KT,
                    classLoader,
                    "asUnlockUsageDetails",
                    usageClass,
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(
                                MethodHookParam param
                        ) {
                            try {
                                Object result =
                                        param.getResult();

                                HookLog.i(
                                        "unlock"
                                                + ", count="
                                                + ReflectUtils.callInt(
                                                result,
                                                "getUnlockTimes",
                                                -1
                                        )
                                                + ", firstTime="
                                                + ReflectUtils.callLong(
                                                result,
                                                "getFirstTime",
                                                -1L
                                        )
                                                + ", buckets="
                                                + ReflectUtils.sizeOf(
                                                ReflectUtils.call(
                                                        result,
                                                        "getUnlocks"
                                                )
                                        )
                                );
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "unlock probe failed",
                                        throwable
                                );
                            }
                        }
                    }
            );

            HookLog.i(
                    "hooked unlock conversion"
            );
        } catch (Throwable throwable) {
            HookLog.e(
                    "unlock hook install failed",
                    throwable
            );
        }
    }

    private static void hookCentralLoader(
            ClassLoader classLoader
    ) {
        try {
            Class<?> loaderClass =
                    XposedHelpers.findClass(
                            HookTargets.CENTRAL_LOADER,
                            classLoader
                    );

            XposedHelpers.findAndHookMethod(
                    loaderClass,
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
                                    return;
                                }

                                HookLog.i(
                                        "query"
                                                + ", start="
                                                + ReflectUtils.callLong(
                                                query,
                                                "getStartTime",
                                                -1L
                                        )
                                                + ", end="
                                                + ReflectUtils.callLong(
                                                query,
                                                "getEndTime",
                                                -1L
                                        )
                                                + ", type="
                                                + ReflectUtils.callString(
                                                query,
                                                "getDateType"
                                        )
                                );
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "query probe failed",
                                        throwable
                                );
                            }
                        }

                        @Override
                        protected void afterHookedMethod(
                                MethodHookParam param
                        ) {
                            try {
                                Object result =
                                        param.getResult();

                                if (!HookTargets.SCREEN_TIME_DETAILS
                                        .equals(
                                                ReflectUtils.className(
                                                        result
                                                )
                                        )) {
                                    return;
                                }

                                Object query =
                                        ReflectUtils.getField(
                                                param.thisObject,
                                                "g"
                                        );

                                Object modified =
                                        StaticModifier.modify(
                                                result,
                                                query,
                                                classLoader
                                        );

                                if (modified != result) {
                                    param.setResult(
                                            modified
                                    );
                                }
                            } catch (Throwable throwable) {
                                HookLog.e(
                                        "central modifier failed",
                                        throwable
                                );
                            }
                        }
                    }
            );

            HookLog.i(
                    "hooked central loader"
            );
        } catch (Throwable throwable) {
            HookLog.e(
                    "central hook install failed",
                    throwable
            );
        }
    }
}
