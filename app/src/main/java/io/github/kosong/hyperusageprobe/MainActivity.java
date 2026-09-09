package io.github.kosong.hyperusageprobe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.kosong.hyperusageprobe.config.ConfigReader;
import io.github.kosong.hyperusageprobe.config.ModuleConfig;
import io.github.kosong.hyperusageprobe.config.UsageRule;
import io.github.kosong.hyperusageprobe.hook.HookTargets;
import io.github.kosong.hyperusageprobe.log.LogFileManager;

public final class MainActivity extends Activity {

    private static final int BACKGROUND =
            Color.rgb(16, 19, 24);

    private static final int CARD =
            Color.rgb(31, 36, 45);

    private static final int TEXT =
            Color.rgb(238, 241, 247);

    private static final int SECONDARY =
            Color.rgb(174, 183, 198);

    private CheckBox enabledCheckBox;
    private CheckBox allDatesCheckBox;
    private CheckBox unlockEnabledCheckBox;
    private CheckBox logEnabledCheckBox;

    private EditText unlockCountEditText;
    private EditText retentionDaysEditText;

    private LinearLayout rulesContainer;

    private final List<RuleRow> ruleRows =
            new ArrayList<>();

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(
                savedInstanceState
        );

        setTitle(
                "Hyper Usage Probe"
        );

        setContentView(
                createContent()
        );

        loadConfigToUi();
    }

    private View createContent() {
        ScrollView scrollView =
                new ScrollView(this);

        scrollView.setBackgroundColor(
                BACKGROUND
        );

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(18),
                dp(22),
                dp(18),
                dp(32)
        );

        scrollView.addView(
                root,
                new ScrollView.LayoutParams(
                        ScrollView.LayoutParams.MATCH_PARENT,
                        ScrollView.LayoutParams.WRAP_CONTENT
                )
        );

        TextView title =
                createText(
                        "Hyper Usage Probe",
                        26,
                        TEXT
                );

        root.addView(title);

        TextView target =
                createText(
                        "目标包："
                                + HookTargets.TARGET_PACKAGE
                                + "\n目标版本："
                                + HookTargets.TARGET_VERSION
                                + "\n\n当前版本支持：\n"
                                + "• DAY 视图解锁次数修改\n"
                                + "• DAY 视图 App 固定时长\n"
                                + "• 自有日志保存、查看和导出\n\n"
                                + "WEEK/MONTH 规则将在当前闭环验证后加入。",
                        15,
                        SECONDARY
                );

        addTop(
                root,
                target,
                14
        );

        enabledCheckBox =
                new CheckBox(this);

        enabledCheckBox.setText(
                "启用数据修改"
        );

        enabledCheckBox.setTextColor(
                TEXT
        );

        addTop(
                root,
                enabledCheckBox,
                18
        );

        allDatesCheckBox =
                new CheckBox(this);

        allDatesCheckBox.setText(
                "应用到所有历史日期"
        );

        allDatesCheckBox.setTextColor(
                TEXT
        );

        addTop(
                root,
                allDatesCheckBox,
                4
        );

        unlockEnabledCheckBox =
                new CheckBox(this);

        unlockEnabledCheckBox.setText(
                "修改今日解锁次数"
        );

        unlockEnabledCheckBox.setTextColor(
                TEXT
        );

        addTop(
                root,
                unlockEnabledCheckBox,
                4
        );

        unlockCountEditText =
                createNumberEditText(
                        "解锁次数",
                        3
                );

        addTop(
                root,
                unlockCountEditText,
                4
        );

        TextView appTitle =
                createText(
                        "App 使用时长规则",
                        19,
                        TEXT
                );

        addTop(
                root,
                appTitle,
                22
        );

        rulesContainer =
                new LinearLayout(this);

        rulesContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        rulesContainer.setBackgroundColor(
                CARD
        );

        rulesContainer.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
        );

        addTop(
                root,
                rulesContainer,
                8
        );

        Button addRule =
                new Button(this);

        addRule.setText(
                "添加 App 规则"
        );

        addRule.setAllCaps(
                false
        );

        addRule.setOnClickListener(
                view -> addRuleRow(
                        "",
                        10L * 60L * 1000L,
                        true
                )
        );

        addTop(
                root,
                addRule,
                10
        );

        logEnabledCheckBox =
                new CheckBox(this);

        logEnabledCheckBox.setText(
                "保存模块日志"
        );

        logEnabledCheckBox.setTextColor(
                TEXT
        );

        addTop(
                root,
                logEnabledCheckBox,
                22
        );

        retentionDaysEditText =
                createNumberEditText(
                        "日志保留天数",
                        3
                );

        addTop(
                root,
                retentionDaysEditText,
                4
        );

        Button save =
                new Button(this);

        save.setText(
                "保存配置"
        );

        save.setAllCaps(
                false
        );

        save.setOnClickListener(
                view -> saveConfig()
        );

        addTop(
                root,
                save,
                18
        );

        Button viewLogs =
                new Button(this);

        viewLogs.setText(
                "查看模块日志"
        );

        viewLogs.setAllCaps(
                false
        );

        viewLogs.setOnClickListener(
                view -> showLogs()
        );

        addTop(
                root,
                viewLogs,
                8
        );

        Button clearLogs =
                new Button(this);

        clearLogs.setText(
                "立即清空模块日志"
        );

        clearLogs.setAllCaps(
                false
        );

        clearLogs.setOnClickListener(
                view -> {
                    LogFileManager.clear(
                            this
                    );

                    Toast.makeText(
                            this,
                            "模块日志已清空",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        addTop(
                root,
                clearLogs,
                8
        );

        Button targetInfo =
                new Button(this);

        targetInfo.setText(
                "打开小米设置应用信息"
        );

        targetInfo.setAllCaps(
                false
        );

        targetInfo.setOnClickListener(
                view -> openTargetInfo()
        );

        addTop(
                root,
                targetInfo,
                8
        );

        Button screenTime =
                new Button(this);

        screenTime.setText(
                "打开屏幕使用时长统计"
        );

        screenTime.setAllCaps(
                false
        );

        screenTime.setOnClickListener(
                view -> openScreenTime()
        );

        addTop(
                root,
                screenTime,
                8
        );

        return scrollView;
    }

    private void addRuleRow(
            String packageName,
            long durationMillis,
            boolean enabled
    ) {
        RuleRow row =
                new RuleRow(
                        this,
                        packageName,
                        durationMillis,
                        enabled
                );

        ruleRows.add(row);

        rulesContainer.addView(
                row.createView()
        );
    }

    private void loadConfigToUi() {
        ModuleConfig config =
                ConfigReader.readForApp(
                        this
                );

        enabledCheckBox.setChecked(
                config.isEnabled()
        );

        allDatesCheckBox.setChecked(
                config.isApplyAllDates()
        );

        unlockEnabledCheckBox.setChecked(
                config.isUnlockEnabled()
        );

        unlockCountEditText.setText(
                String.valueOf(
                        config.getUnlockCount()
                )
        );

        logEnabledCheckBox.setChecked(
                config.isLogEnabled()
        );

        retentionDaysEditText.setText(
                String.valueOf(
                        config.getLogRetentionDays()
                )
        );

        for (UsageRule rule :
                config.getUsageRules()) {
            addRuleRow(
                    rule.getPackageName(),
                    rule.getDurationMillis(),
                    rule.isEnabled()
            );
        }
    }

    private void saveConfig() {
        int unlockCount =
                parseInt(
                        unlockCountEditText.getText()
                                .toString(),
                        3
                );

        int retentionDays =
                parseInt(
                        retentionDaysEditText.getText()
                                .toString(),
                        3
                );

        List<UsageRule> rules =
                new ArrayList<>();

        for (RuleRow row : ruleRows) {
            UsageRule rule =
                    row.toRule();

            if (rule != null) {
                rules.add(rule);
            }
        }

        ModuleConfig config =
                new ModuleConfig(
                        enabledCheckBox.isChecked(),
                        allDatesCheckBox.isChecked(),
                        unlockEnabledCheckBox.isChecked(),
                        unlockCount,
                        logEnabledCheckBox.isChecked(),
                        retentionDays,
                        rules
                );

        ConfigReader.saveForApp(
                this,
                config
        );

        LogFileManager.cleanup(
                this
        );

        Toast.makeText(
                this,
                "配置已保存。请强行停止小米设置后重新打开统计页。",
                Toast.LENGTH_LONG
        ).show();
    }

    private void showLogs() {
        String logs =
                LogFileManager.readAll(
                        this
                );

        TextView textView =
                createText(
                        logs,
                        13,
                        TEXT
                );

        textView.setTextIsSelectable(
                true
        );

        ScrollView scroll =
                new ScrollView(this);

        scroll.addView(
                textView,
                new ScrollView.LayoutParams(
                        ScrollView.LayoutParams.MATCH_PARENT,
                        ScrollView.LayoutParams.WRAP_CONTENT
                )
        );

        new AlertDialog.Builder(this)
                .setTitle(
                        "HyperUsageProbe 日志"
                )
                .setView(
                        scroll
                )
                .setPositiveButton(
                        "关闭",
                        null
                )
                .setNeutralButton(
                        "分享",
                        (dialog, which) ->
                                shareLogs(logs)
                )
                .show();
    }

    private void shareLogs(
            String logs
    ) {
        Intent intent =
                new Intent(
                        Intent.ACTION_SEND
                );

        intent.setType(
                "text/plain"
        );

        intent.putExtra(
                Intent.EXTRA_SUBJECT,
                "HyperUsageProbe 日志"
        );

        intent.putExtra(
                Intent.EXTRA_TEXT,
                logs
        );

        startActivity(
                Intent.createChooser(
                        intent,
                        "分享日志"
                )
        );
    }

    private void openTargetInfo() {
        Intent intent =
                new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                );

        intent.setData(
                Uri.parse(
                        "package:"
                                + HookTargets.TARGET_PACKAGE
                )
        );

        try {
            startActivity(intent);
        } catch (Throwable throwable) {
            Toast.makeText(
                    this,
                    "无法打开小米设置应用信息",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openScreenTime() {
        Intent intent =
                new Intent();

        intent.setClassName(
                HookTargets.TARGET_PACKAGE,
                "com.xiaomi.misettings.usagestats."
                        + "UsageStatsMainActivity"
        );

        try {
            startActivity(intent);
            return;
        } catch (ActivityNotFoundException ignored) {
        } catch (Throwable ignored) {
        }

        try {
            startActivity(
                    new Intent(
                            "miui.action.usagestas.MAIN"
                    )
            );
        } catch (Throwable throwable) {
            Toast.makeText(
                    this,
                    "无法打开屏幕使用时长统计",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private EditText createNumberEditText(
            String hint,
            int defaultValue
    ) {
        EditText editText =
                new EditText(this);

        editText.setHint(
                hint
        );

        editText.setText(
                String.valueOf(
                        defaultValue
                )
        );

        editText.setTextColor(
                TEXT
        );

        editText.setHintTextColor(
                SECONDARY
        );

        editText.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );

        return editText;
    }

    private TextView createText(
            String value,
            int size,
            int color
    ) {
        TextView textView =
                new TextView(this);

        textView.setText(
                value
        );

        textView.setTextSize(
                size
        );

        textView.setTextColor(
                color
        );

        textView.setGravity(
                Gravity.START
        );

        textView.setLineSpacing(
                0.0f,
                1.15f
        );

        return textView;
    }

    private void addTop(
            LinearLayout parent,
            View view,
            int marginTop
    ) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.topMargin =
                dp(marginTop);

        parent.addView(
                view,
                params
        );
    }

    private int dp(
            int value
    ) {
        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private int parseInt(
            String value,
            int fallback
    ) {
        try {
            return Math.max(
                    0,
                    Integer.parseInt(
                            value.trim()
                    )
            );
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static final class RuleRow {

        private final MainActivity activity;
        private final LinearLayout root;
        private final EditText packageEditText;
        private final EditText durationEditText;
        private final CheckBox enabledCheckBox;

        private RuleRow(
                MainActivity activity,
                String packageName,
                long durationMillis,
                boolean enabled
        ) {
            this.activity =
                    activity;

            this.root =
                    new LinearLayout(activity);

            this.root.setOrientation(
                    LinearLayout.VERTICAL
            );

            this.packageEditText =
                    new EditText(activity);

            this.packageEditText.setHint(
                    "包名，例如 com.tencent.mm"
            );

            this.packageEditText.setText(
                    packageName
            );

            this.packageEditText.setTextColor(
                    TEXT
            );

            this.packageEditText.setHintTextColor(
                    SECONDARY
            );

            this.durationEditText =
                    new EditText(activity);

            this.durationEditText.setHint(
                    "固定时长，单位分钟"
            );

            this.durationEditText.setText(
                    String.valueOf(
                            Math.max(
                                    0L,
                                    durationMillis
                            ) / 60000L
                    )
            );

            this.durationEditText.setTextColor(
                    TEXT
            );

            this.durationEditText.setHintTextColor(
                    SECONDARY
            );

            this.durationEditText.setInputType(
                    android.text.InputType.TYPE_CLASS_NUMBER
            );

            this.enabledCheckBox =
                    new CheckBox(activity);

            this.enabledCheckBox.setText(
                    "启用此 App 规则"
            );

            this.enabledCheckBox.setTextColor(
                    TEXT
            );

            this.enabledCheckBox.setChecked(
                    enabled
            );

            Button remove =
                    new Button(activity);

            remove.setText(
                    "删除此规则"
            );

            remove.setAllCaps(
                    false
            );

            remove.setOnClickListener(
                    view -> {
                        activity.ruleRows.remove(
                                this
                        );

                        activity.rulesContainer
                                .removeView(
                                        root
                                );
                    }
            );

            root.addView(
                    packageEditText
            );

            root.addView(
                    durationEditText
            );

            root.addView(
                    enabledCheckBox
            );

            root.addView(
                    remove
            );
        }

        private View createView() {
            return root;
        }

        private UsageRule toRule() {
            String packageName =
                    packageEditText.getText()
                            .toString()
                            .trim();

            if (packageName.isEmpty()) {
                return null;
            }

            int minutes =
                    0;

            try {
                minutes =
                        Math.max(
                                0,
                                Integer.parseInt(
                                        durationEditText
                                                .getText()
                                                .toString()
                                                .trim()
                                )
                        );
            } catch (Throwable ignored) {
            }

            return new UsageRule(
                    packageName,
                    minutes * 60L * 1000L,
                    enabledCheckBox.isChecked()
            );
        }
    }
}
