package io.github.kosong.hyperusageprobe.hook;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedHelpers;

import io.github.kosong.hyperusageprobe.config.ConfigReader;
import io.github.kosong.hyperusageprobe.config.ModuleConfig;
import io.github.kosong.hyperusageprobe.config.UsageRule;
import io.github.kosong.hyperusageprobe.util.HookLog;
import io.github.kosong.hyperusageprobe.util.ReflectUtils;

public final class StaticModifier {

    private static final String DAY =
            "DAY";

    private static final String SCREEN_TIME_DETAILS =
            "com.xiaomi.misettings.base.model.page."
                    + "ScreenTimeDetails";

    private static final String DEVICE_USAGE_DETAILS =
            "com.xiaomi.misettings.base.model.page."
                    + "ScreenTimeDetails$DeviceUsageDetails";

    private static final String NAME_CATEGORY_DETAILS =
            "com.xiaomi.misettings.base.model.page."
                    + "ScreenTimeDetails$NameAndCategoryDetails";

    private static final String UNLOCK_USAGE_DETAILS =
            "com.xiaomi.misettings.base.model.page."
                    + "ScreenTimeDetails$UnlockUsageDetails";

    private StaticModifier() {
    }

    public static Object modify(
            Object original,
            Object query,
            ClassLoader classLoader
    ) {
        if (original == null
                || query == null
                || classLoader == null) {
            return original;
        }

        if (!SCREEN_TIME_DETAILS.equals(
                original.getClass().getName()
        )) {
            return original;
        }

        ModuleConfig config =
                ConfigReader.readForHook();

        if (!config.isEnabled()) {
            return original;
        }

        String dateType =
                ReflectUtils.callString(
                        query,
                        "getDateType"
                );

        if (!DAY.equals(dateType)) {
            return original;
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

        boolean apply =
                config.isApplyAllDates()
                        || isToday(
                                startTime,
                                endTime
                        );

        if (!apply) {
            return original;
        }

        Object deviceUsage =
                ReflectUtils.call(
                        original,
                        "getDeviceUsage"
                );

        Object names =
                ReflectUtils.call(
                        original,
                        "getNameAndCategoryDetails"
                );

        Object unlockUsage =
                ReflectUtils.call(
                        original,
                        "getUnlockUsage"
                );

        Object newNames =
                modifyAppList(
                        names,
                        config,
                        classLoader
                );

        Object newUnlockUsage =
                modifyUnlock(
                        unlockUsage,
                        config,
                        classLoader
                );

        boolean changed =
                newNames != names
                        || newUnlockUsage != unlockUsage;

        if (!changed) {
            return original;
        }

        try {
            Class<?> detailsClass =
                    XposedHelpers.findClass(
                            SCREEN_TIME_DETAILS,
                            classLoader
                    );

            Object result =
                    XposedHelpers.newInstance(
                            detailsClass,
                            deviceUsage,
                            newNames,
                            newUnlockUsage
                    );

            HookLog.i(
                    "screen details replaced"
                            + ", dateType=" + dateType
                            + ", startTime=" + startTime
                            + ", endTime=" + endTime
            );

            return result;
        } catch (Throwable throwable) {
            HookLog.e(
                    "create ScreenTimeDetails failed",
                    throwable
            );

            return original;
        }
    }

    private static Object modifyAppList(
            Object original,
            ModuleConfig config,
            ClassLoader classLoader
    ) {
        if (original == null) {
            return null;
        }

        Object appDetails =
                ReflectUtils.call(
                        original,
                        "getAppDetails"
                );

        Object categoryDetails =
                ReflectUtils.call(
                        original,
                        "getCategoryDetails"
                );

        if (!(appDetails instanceof List)) {
            return original;
        }

        List<?> source =
                (List<?>) appDetails;

        List<Object> modified =
                new ArrayList<>(
                        source.size()
                );

        boolean changed =
                false;

        for (Object item : source) {
            Object newItem =
                    modifyItem(
                            item,
                            config
                    );

            if (newItem != item) {
                changed = true;
            }

            modified.add(newItem);
        }

        if (!changed) {
            return original;
        }

        try {
            Class<?> detailsClass =
                    XposedHelpers.findClass(
                            NAME_CATEGORY_DETAILS,
                            classLoader
                    );

            return XposedHelpers.newInstance(
                    detailsClass,
                    modified,
                    categoryDetails
            );
        } catch (Throwable throwable) {
            HookLog.e(
                    "create NameAndCategoryDetails failed",
                    throwable
            );

            return original;
        }
    }

    private static Object modifyItem(
            Object item,
            ModuleConfig config
    ) {
        if (item == null) {
            return item;
        }

        String packageName =
                ReflectUtils.callString(
                        item,
                        "getPackageName"
                );

        if (packageName == null
                || packageName.isEmpty()) {
            return item;
        }

        UsageRule rule =
                findRule(
                        config,
                        packageName
                );

        if (rule == null
                || !rule.isEnabled()) {
            return item;
        }

        Object appItem =
                ReflectUtils.call(
                        item,
                        "getDetail"
                );

        if (appItem == null) {
            return item;
        }

        long oldUsage =
                ReflectUtils.callLong(
                        appItem,
                        "getUsage",
                        -1L
                );

        long newUsage =
                rule.getDurationMillis();

        if (oldUsage == newUsage) {
            return item;
        }

        try {
            Object newAppItem =
                    XposedHelpers.callMethod(
                            appItem,
                            "copy",
                            ReflectUtils.callString(
                                    appItem,
                                    "getTitle"
                            ),
                            Long.valueOf(newUsage),
                            ReflectUtils.callString(
                                    appItem,
                                    "getIcon"
                            ),
                            ReflectUtils.call(
                                    appItem,
                                    "getDrawable"
                            ),
                            Boolean.valueOf(
                                    ReflectUtils.call(
                                            appItem,
                                            "isLimit"
                                    ) instanceof Boolean
                                            && (Boolean) ReflectUtils.call(
                                            appItem,
                                            "isLimit"
                                    )
                            ),
                            Boolean.valueOf(
                                    ReflectUtils.call(
                                            appItem,
                                            "isSystem"
                                    ) instanceof Boolean
                                            && (Boolean) ReflectUtils.call(
                                            appItem,
                                            "isSystem"
                                    )
                            ),
                            ReflectUtils.call(
                                    appItem,
                                    "getDataType"
                            ),
                            Boolean.valueOf(
                                    ReflectUtils.call(
                                            appItem,
                                            "getAsTitle"
                                    ) instanceof Boolean
                                            && (Boolean) ReflectUtils.call(
                                            appItem,
                                            "getAsTitle"
                                    )
                            ),
                            ReflectUtils.callString(
                                    appItem,
                                    "getKeywords"
                            )
                    );

            Object newItem =
                    XposedHelpers.callMethod(
                            item,
                            "copy",
                            newAppItem,
                            ReflectUtils.callString(
                                    item,
                                    "getPackageName"
                            ),
                            ReflectUtils.callString(
                                    item,
                                    "getCategoryType"
                            ),
                            ReflectUtils.callString(
                                    item,
                                    "getCategoryId"
                            ),
                            ReflectUtils.call(
                                    item,
                                    "getAppType"
                            ),
                            ReflectUtils.call(
                                    item,
                                    "getGroup"
                            ),
                            Boolean.valueOf(
                                    ReflectUtils.call(
                                            item,
                                            "getPressEffect"
                                    ) instanceof Boolean
                                            && (Boolean) ReflectUtils.call(
                                            item,
                                            "getPressEffect"
                                    )
                            )
                    );

            HookLog.i(
                    "modified app"
                            + ", pkg=" + packageName
                            + ", oldMillis=" + oldUsage
                            + ", newMillis=" + newUsage
            );

            return newItem;
        } catch (Throwable throwable) {
            HookLog.e(
                    "modify app failed: " + packageName,
                    throwable
            );

            return item;
        }
    }

    private static Object modifyUnlock(
            Object original,
            ModuleConfig config,
            ClassLoader classLoader
    ) {
        if (original == null
                || !config.isUnlockEnabled()) {
            return original;
        }

        int target =
                config.getUnlockCount();

        List<?> oldDistribution =
                (List<?>) ReflectUtils.call(
                        original,
                        "getUnlocks"
                );

        int size =
                oldDistribution == null
                        ? 24
                        : oldDistribution.size();

        List<Integer> distribution =
                new ArrayList<>(
                        size
                );

        for (int index = 0; index < size; index++) {
            distribution.add(0);
        }

        if (!distribution.isEmpty()
                && target > 0) {
            distribution.set(
                    distribution.size() - 1,
                    target
            );
        }

        int maxValue = 0;

        for (Integer value : distribution) {
            if (value != null) {
                maxValue =
                        Math.max(
                                maxValue,
                                value
                        );
            }
        }

        long firstTime =
                ReflectUtils.callLong(
                        original,
                        "getFirstTime",
                        0L
                );

        int lastCycle =
                ReflectUtils.callInt(
                        original,
                        "getLastCycle",
                        0
                );

        try {
            Class<?> detailsClass =
                    XposedHelpers.findClass(
                            UNLOCK_USAGE_DETAILS,
                            classLoader
                    );

            Object result =
                    XposedHelpers.newInstance(
                            detailsClass,
                            Integer.valueOf(target),
                            distribution,
                            Integer.valueOf(maxValue),
                            Long.valueOf(firstTime),
                            Integer.valueOf(target),
                            Integer.valueOf(lastCycle)
                    );

            HookLog.i(
                    "modified unlock"
                            + ", count=" + target
                            + ", maxValue=" + maxValue
            );

            return result;
        } catch (Throwable throwable) {
            HookLog.e(
                    "modify unlock failed",
                    throwable
            );

            return original;
        }
    }

    private static UsageRule findRule(
            ModuleConfig config,
            String packageName
    ) {
        for (UsageRule rule :
                config.getUsageRules()) {
            if (packageName.equals(
                    rule.getPackageName()
            )) {
                return rule;
            }
        }

        return null;
    }

    private static boolean isToday(
            long startTime,
            long endTime
    ) {
        if (startTime <= 0L
                || endTime <= 0L) {
            return false;
        }

        long now =
                System.currentTimeMillis();

        return startTime <= now
                && now < endTime;
    }
}
