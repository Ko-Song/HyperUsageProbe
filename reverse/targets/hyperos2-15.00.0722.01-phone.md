# HyperOS 2 / Xiaomi MiSettings 15.00.0722.01-phone

## Target

- Package: `com.xiaomi.misettings`
- Version: `15.00.0722.01-phone`

## Entry

- Alias:
  `com.xiaomi.misettings.usagestats.UsageStatsMainActivity`
- Activity:
  `com.xiaomi.misettings.features.screentime.ui.ScreenTimeDetailPage`
- Fragment:
  `com.xiaomi.misettings.features.screentime.ui.ScreenTimeDetailFragment`

## Main data flow

- ViewModel: `ma.h0`
- Central coroutine: `ma.i0`
- Query: `ScreenTimeCommonQuery`
- Page model: `ScreenTimeDetails`

## Stable conversion hooks

- `DeviceAppUsageKt.asDeviceUsageDetails`
- `DeviceAppUsageKt.asNameAndCategoryDetails`
- `UnlockUsageKt.asUnlockUsageDetails`

## Unlock source

- Repository: `ea.i`
- Source file: `UnlockUsageRepository.kt`
- Android API: `UsageStatsManager.queryEvents`
- Unlock event: `eventType == 18`

## Current stage

`0.1.0-probe` only logs data and does not modify results.