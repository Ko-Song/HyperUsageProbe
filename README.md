# HyperUsageProbe

小米 HyperOS 2 屏幕使用时长数据的 LSPosed 探针。

## 当前阶段

版本 `0.1.0-probe`：

- 只记录，不修改
- 目标包：`com.xiaomi.misettings`
- 已适配目标版本：`15.00.0722.01-phone`

## 构建

1. 打开仓库的 Actions 页面。
2. 选择 `Build APK`。
3. 运行工作流。
4. 下载 `HyperUsageProbe-debug`。
5. 解压并安装 `app-debug.apk`。

## 使用

1. 在 LSPosed 中启用模块。
2. 应用推荐作用域：`com.xiaomi.misettings`。
3. 强行停止小米设置。
4. 打开屏幕使用时长统计。
5. 在 LSPosed 日志中搜索 `HyperUsageProbe`。

## 注意

日志可能包含设备使用时长和解锁统计，公开前请脱敏。