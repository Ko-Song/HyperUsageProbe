package io.github.kosong.hyperusageprobe.hook;

/**
 * 当前已确认的小米设置目标。
 *
 * 该文件只保存目标名称，不包含任何修改逻辑。
 */
public final class HookTargets {

    private HookTargets() {
    }

    public static final String TARGET_PACKAGE =
            "com.xiaomi.misettings";

    public static final String TARGET_VERSION =
            "15.00.0722.01-phone";

    public static final String DEVICE_APP_USAGE_KT =
            "com.xiaomi.misettings.features.screentime.data.model.DeviceAppUsageKt";

    public static final String DEVICE_APP_USAGE =
            "com.xiaomi.misettings.features.screentime.data.model.DeviceAppUsage";

    public static final String APP_USAGE =
            "com.xiaomi.misettings.features.screentime.data.model.AppUsage";

    public static final String UNLOCK_USAGE_KT =
            "com.xiaomi.misettings.features.screentime.data.model.UnlockUsageKt";

    public static final String UNLOCK_USAGE =
            "com.xiaomi.misettings.features.screentime.data.model.UnlockUsage";

    public static final String SCREEN_TIME_QUERY =
            "com.xiaomi.misettings.features.screentime.data.model.ScreenTimeCommonQuery";

    public static final String SCREEN_TIME_DETAILS =
            "com.xiaomi.misettings.base.model.page.ScreenTimeDetails";

    /*
     * 当前目标 APK 中的混淆内部协程类。
     * 源码名：
     * ScreenTimeDetailViewModel$getScreenTimeDetail$2
     */
    public static final String CENTRAL_LOADER =
            "ma.i0";

    /*
     * 探针默认只详细记录微信，避免把全部 App 使用情况写入日志。
     */
    public static final String PROBE_APP_PACKAGE =
            "com.tencent.mm";
} 