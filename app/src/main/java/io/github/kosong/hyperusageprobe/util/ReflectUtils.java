package io.github.kosong.hyperusageprobe.util;

import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;

public final class ReflectUtils {

    private ReflectUtils() {
    }

    public static Object call(Object target, String methodName) {
        if (target == null) {
            return null;
        }

        try {
            return XposedHelpers.callMethod(target, methodName);
        } catch (Throwable throwable) {
            HookLog.e(
                    "call failed: "
                            + target.getClass().getName()
                            + "#"
                            + methodName,
                    throwable
            );
            return null;
        }
    }

    public static long callLong(
            Object target,
            String methodName,
            long fallback
    ) {
        Object value = call(target, methodName);

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return fallback;
    }

    public static int callInt(
            Object target,
            String methodName,
            int fallback
    ) {
        Object value = call(target, methodName);

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return fallback;
    }

    public static String callString(
            Object target,
            String methodName
    ) {
        Object value = call(target, methodName);
        return value == null ? null : String.valueOf(value);
    }

    public static Object getField(
            Object target,
            String fieldName
    ) {
        if (target == null) {
            return null;
        }

        try {
            return XposedHelpers.getObjectField(target, fieldName);
        } catch (Throwable throwable) {
            HookLog.e(
                    "getField failed: "
                            + target.getClass().getName()
                            + "."
                            + fieldName,
                    throwable
            );
            return null;
        }
    }

    public static int sizeOf(Object object) {
        if (object instanceof List) {
            return ((List<?>) object).size();
        }

        if (object instanceof Map) {
            return ((Map<?, ?>) object).size();
        }

        return -1;
    }

    public static String limitedList(
            Object object,
            int maxItems
    ) {
        if (!(object instanceof List)) {
            return String.valueOf(object);
        }

        List<?> list = (List<?>) object;
        StringBuilder builder = new StringBuilder();

        builder.append('[');

        int count = Math.min(list.size(), Math.max(0, maxItems));

        for (int index = 0; index < count; index++) {
            if (index > 0) {
                builder.append(", ");
            }

            builder.append(String.valueOf(list.get(index)));
        }

        if (list.size() > count) {
            builder.append(", ... total=");
            builder.append(list.size());
        }

        builder.append(']');
        return builder.toString();
    }

    public static String className(Object object) {
        return object == null
                ? "null"
                : object.getClass().getName();
    }
} 