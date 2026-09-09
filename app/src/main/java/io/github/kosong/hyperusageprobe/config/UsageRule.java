package io.github.kosong.hyperusageprobe.config;

import org.json.JSONException;
import org.json.JSONObject;

public final class UsageRule {

    private final String packageName;
    private final long durationMillis;
    private final boolean enabled;

    public UsageRule(
            String packageName,
            long durationMillis,
            boolean enabled
    ) {
        this.packageName =
                packageName == null ? "" : packageName.trim();

        this.durationMillis =
                Math.max(0L, durationMillis);

        this.enabled = enabled;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();

        object.put(
                "packageName",
                packageName
        );

        object.put(
                "durationMillis",
                durationMillis
        );

        object.put(
                "enabled",
                enabled
        );

        return object;
    }

    public static UsageRule fromJson(
            JSONObject object
    ) {
        if (object == null) {
            return null;
        }

        String packageName =
                object.optString(
                        "packageName",
                        ""
                ).trim();

        if (packageName.isEmpty()) {
            return null;
        }

        long durationMillis =
                object.optLong(
                        "durationMillis",
                        0L
                );

        boolean enabled =
                object.optBoolean(
                        "enabled",
                        true
                );

        return new UsageRule(
                packageName,
                durationMillis,
                enabled
        );
    }
}
