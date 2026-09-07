package io.github.kosong.hyperusageprobe;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import io.github.kosong.hyperusageprobe.hook.HookTargets;

public final class MainActivity extends Activity {

    private static final int COLOR_BACKGROUND =
            Color.rgb(16, 19, 24);

    private static final int COLOR_CARD =
            Color.rgb(31, 36, 45);

    private static final int COLOR_TEXT =
            Color.rgb(235, 238, 245);

    private static final int COLOR_SECONDARY =
            Color.rgb(170, 178, 192);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Hyper Usage Probe");
        setContentView(createContent());
    }

    private View createContent() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(COLOR_BACKGROUND);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(32));

        scrollView.addView(
                root,
                new ScrollView.LayoutParams(
                        ScrollView.LayoutParams.MATCH_PARENT,
                        ScrollView.LayoutParams.WRAP_CONTENT
                )
        );

        TextView title = text(
                "Hyper Usage Probe",
                26,
                COLOR_TEXT
        );
        title.setGravity(Gravity.START);
        root.addView(title);

        TextView version = text(
                "模块版本：0.1.0-probe",
                15,
                COLOR_SECONDARY
        );
        addWithTopMargin(root, version, 8);

        TextView status = text(
                "当前阶段：只记录，不修改数据\n\n"
                        + "目标包："
                        + HookTargets.TARGET_PACKAGE
                        + "\n目标小米设置版本："
                        + HookTargets.TARGET_VERSION
                        + "\n\n"
                        + "已设计的探针：\n"
                        + "• 总屏幕使用时长\n"
                        + "• App 使用列表\n"
                        + "• 微信使用时长\n"
                        + "• 解锁次数与首次解锁时间\n"
                        + "• 页面查询日期区间\n\n"
                        + "请在 LSPosed 中启用模块，并将作用域"
                        + "仅勾选“小米设置”。",
                16,
                COLOR_TEXT
        );
        status.setBackgroundColor(COLOR_CARD);
        status.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );
        addWithTopMargin(root, status, 20);

        Button openScreenTime = new Button(this);
        openScreenTime.setText("打开屏幕使用时长统计");
        openScreenTime.setAllCaps(false);
        openScreenTime.setOnClickListener(
                view -> openScreenTimePage()
        );
        addWithTopMargin(root, openScreenTime, 20);

        Button openTargetInfo = new Button(this);
        openTargetInfo.setText("打开小米设置应用信息");
        openTargetInfo.setAllCaps(false);
        openTargetInfo.setOnClickListener(
                view -> openTargetAppInfo()
        );
        addWithTopMargin(root, openTargetInfo, 12);

        TextView warning = text(
                "注意：探针日志会包含本机的屏幕使用时长、"
                        + "解锁次数以及目标 App 使用时间。"
                        + "提交日志前请检查并删除不希望公开的信息。",
                14,
                COLOR_SECONDARY
        );
        addWithTopMargin(root, warning, 20);

        return scrollView;
    }

    private TextView text(
            String value,
            int sizeSp,
            int color
    ) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(sizeSp);
        textView.setTextColor(color);
        textView.setLineSpacing(0.0f, 1.15f);
        return textView;
    }

    private void addWithTopMargin(
            LinearLayout root,
            View view,
            int topDp
    ) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(topDp);
        root.addView(view, params);
    }

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private void openScreenTimePage() {
        /*
         * Manifest 中已确认 UsageStatsMainActivity 是别名，
         * 其 targetActivity 是 ScreenTimeDetailPage。
         */
        Intent explicitIntent = new Intent();
        explicitIntent.setClassName(
                HookTargets.TARGET_PACKAGE,
                "com.xiaomi.misettings.usagestats."
                        + "UsageStatsMainActivity"
        );

        try {
            startActivity(explicitIntent);
            return;
        } catch (ActivityNotFoundException ignored) {
            // 尝试兼容 Action。
        } catch (Throwable ignored) {
            // 尝试兼容 Action。
        }

        Intent actionIntent = new Intent(
                "miui.action.usagestas.MAIN"
        );

        try {
            startActivity(actionIntent);
        } catch (Throwable throwable) {
            Toast.makeText(
                    this,
                    "无法打开统计页，请从系统设置中手动进入",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void openTargetAppInfo() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        );

        intent.setData(
                Uri.parse(
                        "package:" + HookTargets.TARGET_PACKAGE
                )
        );

        try {
            startActivity(intent);
        } catch (Throwable throwable) {
            Toast.makeText(
                    this,
                    "无法打开应用信息",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}