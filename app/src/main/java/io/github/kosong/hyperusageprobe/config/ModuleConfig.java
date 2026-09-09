package io.github.kosong.hyperusageprobe.config;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleConfig {

    public static final String PREF_NAME =
            "hyper_usage_config";

    public static final String KEY_CONFIG_JSON =
            "config_json";

    public static final String KEY_ENABLED =
            "enabled";

    public static final String KEY_APPLY_ALL_DATES =
            "apply_all_dates";

    public static final String KEY_UNLOCK_ENABLED =
            "unlock_enabled";

    public static final String KEY_UNLOCK_COUNT =
            "unlock_count";

    public static final String KEY_LOG_ENABLED =
            "log_enabled";

    public static final String KEY_LOG_RETENTION_DAYS =
            "log_retention_days";

    private final boolean enabled;
    private final boolean applyAllDates;
    private final boolean unlockEnabled;
    private final int unlockCount;
    private final boolean logEnabled;
    private final int logRetentionDays;
    private final List<UsageRule> usageRules;

    public ModuleConfig(
            boolean enabled,
            boolean applyAllDates,
            boolean unlockEnabled,
            int unlockCount,
            boolean logEnabled,
            int logRetentionDays,
            List<UsageRule> usageRules
    ) {
        this.enabled = enabled;
        this.applyAllDates = applyAllDates;
        this.unlockEnabled = unlockEnabled;
        this.unlockCount =
                Math.max(0, unlockCount);

        this.logEnabled = logEnabled;
        this.logRetentionDays =
                Math.max(1, logRetentionDays);

        if (usageRules == null) {
            this.usageRules =
                    Collections.emptyList();
        } else {
            this.usageRules =
                    Collections.unmodifiableList(
                            new ArrayList<>(usageRules)
                    );
        }
    }

    public static ModuleConfig defaults() {
        return new ModuleConfig(
                false,
                false,
                false,
                3,
                true,
                3,
                Collections.emptyList()
        );
    }

    public static ModuleConfig load(
            SharedPreferences preferences
    ) {
        if (preferences == null) {
            return defaults();
        }

        String json =
                preferences.getString(
                        KEY_CONFIG_JSON,
                        null
                );

        if (json == null || json.trim().isEmpty()) {
            return defaults();
        }

        try {
            JSONObject root =
                    new JSONObject(json);

            boolean enabled =
                    root.optBoolean(
                            KEY_ENABLED,
                            false
                    );

            boolean applyAllDates =
                    root.optBoolean(
                            KEY_APPLY_ALL_DATES,
                            false
                    );

            boolean unlockEnabled =
                    root.optBoolean(
                            KEY_UNLOCK_ENABLED,
                            false
                    );

            int unlockCount =
                    root.optInt(
                            KEY_UNLOCK_COUNT,
                            3
                    );

            boolean logEnabled =
                    root.optBoolean(
                            KEY_LOG_ENABLED,
                            true
                    );

            int retentionDays =
                    root.optInt(
                            KEY_LOG_RETENTION_DAYS,
                            3
                    );

            JSONArray array =
                    root.optJSONArray(
                            "usageRules"
                    );

            List<UsageRule> rules =
                    new ArrayList<>();

            if (array != null) {
                for (
                        int index = 0;
                        index < array.length();
                        index++
                ) {
                    UsageRule rule =
                            UsageRule.fromJson(
                                    array.optJSONObject(index)
                            );

                    if (rule != null) {
                        rules.add(rule);
                    }
                }
            }

            return new ModuleConfig(
                    enabled,
                    applyAllDates,
                    unlockEnabled,
                    unlockCount,
                    logEnabled,
                    retentionDays,
                    rules
            );
        } catch (JSONException ignored) {
            return defaults();
        }
    }

    public void save(
            SharedPreferences preferences
    ) {
        if (preferences == null) {
            return;
        }

        try {
            JSONObject root =
                    new JSONObject();

            root.put(
                    KEY_ENABLED,
                    enabled
            );

            root.put(
                    KEY_APPLY_ALL_DATES,
                    applyAllDates
            );

            root.put(
                    KEY_UNLOCK_ENABLED,
                    unlockEnabled
            );

            root.put(
                    KEY_UNLOCK_COUNT,
                    unlockCount
            );

            root.put(
                    KEY_LOG_ENABLED,
                    logEnabled
            );

            root.put(
                    KEY_LOG_RETENTION_DAYS,
                    logRetentionDays
            );

            JSONArray array =
                    new JSONArray();

            for (UsageRule rule : usageRules) {
                array.put(rule.toJson());
            }

            root.put(
                    "usageRules",
                    array
            );

            preferences.edit()
                    .putString(
                            KEY_CONFIG_JSON,
                            root.toString()
                    )
                    .apply();
        } catch (JSONException ignored) {
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isApplyAllDates() {
        return applyAllDates;
    }

    public boolean isUnlockEnabled() {
        return unlockEnabled;
    }

    public int getUnlockCount() {
        return unlockCount;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public int getLogRetentionDays() {
        return logRetentionDays;
    }

    public List<UsageRule> getUsageRules() {
        return usageRules;
    }
}
