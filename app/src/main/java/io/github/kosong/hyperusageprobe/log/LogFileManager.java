package io.github.kosong.hyperusageprobe.log;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.kosong.hyperusageprobe.config.ModuleConfig;

public final class LogFileManager {

    private static final String DIRECTORY_NAME =
            "logs";

    private static final String FILE_PREFIX =
            "probe-";

    private static final String FILE_SUFFIX =
            ".log";

    private static final long MAX_FILE_SIZE =
            2L * 1024L * 1024L;

    private static final Object LOCK =
            new Object();

    private LogFileManager() {
    }

    public static File getDirectory(
            Context context
    ) {
        File directory =
                new File(
                        context.getFilesDir(),
                        DIRECTORY_NAME
                );

        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }

        return directory;
    }

    public static void append(
            Context context,
            String line
    ) {
        if (context == null
                || line == null
                || line.isEmpty()) {
            return;
        }

        synchronized (LOCK) {
            try {
                SharedPreferences preferences =
                        context.getSharedPreferences(
                                ModuleConfig.PREF_NAME,
                                Context.MODE_PRIVATE
                        );

                ModuleConfig config =
                        ModuleConfig.load(preferences);

                if (!config.isLogEnabled()) {
                    return;
                }

                File directory =
                        getDirectory(context);

                cleanupExpired(
                        directory,
                        config.getLogRetentionDays()
                );

                File file =
                        getTodayFile(directory);

                appendLine(
                        file,
                        line
                );

                trimFile(
                        file,
                        MAX_FILE_SIZE
                );
            } catch (Throwable ignored) {
            }
        }
    }

    public static void clear(
            Context context
    ) {
        if (context == null) {
            return;
        }

        synchronized (LOCK) {
            File directory =
                    getDirectory(context);

            File[] files =
                    directory.listFiles();

            if (files == null) {
                return;
            }

            for (File file : files) {
                if (file.isFile()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }
    }

    public static String readAll(
            Context context
    ) {
        if (context == null) {
            return "暂无日志";
        }

        synchronized (LOCK) {
            cleanup(context);

            File directory =
                    getDirectory(context);

            File[] files =
                    directory.listFiles();

            if (files == null || files.length == 0) {
                return "暂无日志";
            }

            List<File> sorted =
                    new ArrayList<>(
                            Arrays.asList(files)
                    );

            sorted.sort(
                    Comparator.comparing(
                            File::getName
                    )
            );

            StringBuilder result =
                    new StringBuilder();

            for (File file : sorted) {
                if (!file.isFile()) {
                    continue;
                }

                result.append(
                        "===== "
                ).append(
                        file.getName()
                ).append(
                        " =====\n"
                );

                readFile(
                        file,
                        result
                );

                result.append('\n');
            }

            return result.toString();
        }
    }

    private static File getTodayFile(
            File directory
    ) {
        String date =
                new SimpleDateFormat(
                        "yyyyMMdd",
                        Locale.US
                ).format(new Date());

        return new File(
                directory,
                FILE_PREFIX + date + FILE_SUFFIX
        );
    }

    private static void appendLine(
            File file,
            String line
    ) throws Exception {
        try (
                FileOutputStream output =
                        new FileOutputStream(file, true);

                OutputStreamWriter writer =
                        new OutputStreamWriter(
                                output,
                                StandardCharsets.UTF_8
                        );

                BufferedWriter buffered =
                        new BufferedWriter(writer)
        ) {
            buffered.write(line);
            buffered.newLine();
            buffered.flush();
        }
    }

    private static void readFile(
            File file,
            StringBuilder result
    ) {
        try (
                FileInputStream input =
                        new FileInputStream(file);

                InputStreamReader reader =
                        new InputStreamReader(
                                input,
                                StandardCharsets.UTF_8
                        );

                BufferedReader buffered =
                        new BufferedReader(reader)
        ) {
            String line;

            while ((line = buffered.readLine()) != null) {
                result.append(line).append('\n');
            }
        } catch (Throwable throwable) {
            result.append(
                    "[read failed] "
            ).append(
                    throwable.getClass().getName()
            ).append('\n');
        }
    }

    private static void cleanupExpired(
            File directory,
            int retentionDays
    ) {
        if (retentionDays < 1) {
            retentionDays = 3;
        }

        long deadline =
                System.currentTimeMillis()
                        - retentionDays
                        * 24L
                        * 60L
                        * 60L
                        * 1000L;

        File[] files =
                directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isFile()
                    && file.lastModified() < deadline) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

    private static void trimFile(
            File file,
            long maxBytes
    ) {
        if (!file.exists()
                || file.length() <= maxBytes) {
            return;
        }

        try {
            byte[] data =
                    readBytes(file);

            int keep =
                    (int) Math.min(
                            maxBytes,
                            data.length
                    );

            int start =
                    Math.max(
                            0,
                            data.length - keep
                    );

            File temp =
                    new File(
                            file.getParentFile(),
                            file.getName()
                                    + ".tmp"
                    );

            try (
                    FileOutputStream output =
                            new FileOutputStream(temp)
            ) {
                output.write(
                        data,
                        start,
                        data.length - start
                );
                output.flush();
            }

            //noinspection ResultOfMethodCallIgnored
            file.delete();

            //noinspection ResultOfMethodCallIgnored
            temp.renameTo(file);
        } catch (Throwable ignored) {
        }
    }

    private static byte[] readBytes(
            File file
    ) throws Exception {
        try (FileInputStream input =
                     new FileInputStream(file)) {
            return input.readAllBytes();
        }
    }
}