package io.github.kosong.hyperusageprobe.log;

public final class LogContract {

    public static final String AUTHORITY =
            "io.github.kosong.hyperusageprobe.log";

    public static final String METHOD_WRITE =
            "write";

    public static final String METHOD_CLEAR =
            "clear";

    public static final String METHOD_READ =
            "read";

    public static final String KEY_LINE =
            "line";

    public static final String KEY_TEXT =
            "text";

    public static final String KEY_RESULT =
            "result";

    public static final String RESULT_OK =
            "ok";

    public static final String RESULT_DENIED =
            "denied";

    private LogContract() {
    }
}