---
name: testing-android-inventory
description: Test the inventory-apk Android app, including sync/import UI flows and local Gradle verification.
---

# Android inventory app testing

Use this skill when verifying Android UI or sync/import changes in `afilimets-ai/inventory-apk`.

## Devin Secrets Needed

- None for local Gradle checks or local-folder CSV import testing.
- External sync providers may need provider-specific credentials, but the local-folder import flow does not.

## Environment requirements

- Android SDK is expected at `/home/ubuntu/android-sdk`.
- Run Gradle commands with:
  ```bash
  ANDROID_HOME=/home/ubuntu/android-sdk ANDROID_SDK_ROOT=/home/ubuntu/android-sdk
  ```
- Visual Android E2E requires either:
  - a connected physical Android device visible in `adb devices`, or
  - an emulator with hardware acceleration/KVM available.
- If `/dev/kvm` is absent, an emulator might connect as `adb device` but still fail to expose Android framework services. Check before claiming UI coverage:
  ```bash
  /home/ubuntu/android-sdk/platform-tools/adb devices
  /home/ubuntu/android-sdk/platform-tools/adb shell wm size
  /home/ubuntu/android-sdk/platform-tools/adb shell cmd package list packages | head
  ```
  If these return errors such as `cmd: Can't find service: window` or `cmd: Can't find service: package`, visual UI testing is not trustworthy; report UI assertions as untested and use command evidence only.

## Local verification commands

Primary local check:

```bash
ANDROID_HOME=/home/ubuntu/android-sdk ANDROID_SDK_ROOT=/home/ubuntu/android-sdk bash ./gradlew testDebugUnitTest lintDebug compileDebugKotlin --stacktrace
```

Build a debug APK:

```bash
ANDROID_HOME=/home/ubuntu/android-sdk ANDROID_SDK_ROOT=/home/ubuntu/android-sdk bash ./gradlew assembleDebug --stacktrace
```

## Sync import UI path

- App starts at the scan screen.
- The top-bar sync button is rendered as `⇅` in `ScanStatusBar` and navigates to `Синхронізація`.
- On the sync settings screen:
  - enable import (`ІМП`) for `Локальна папка`,
  - open the local-folder settings gear,
  - keep format `CSV` when testing CSV import,
  - choose a folder containing `inventory_import.csv`,
  - tap `Зберегти`, then `Імпорт`.

## Import-summary test data

For adversarial import-summary testing, use a CSV with valid and invalid rows:

```csv
barcode,name,quantity,unit
4820001112223,Тестовий цукор,5,кг
4820003334445,Тестова кава,12,шт
,Рядок без штрихкоду,7,шт
```

Expected summary:

- `Імпорт завершено`
- `Локальна папка · inventory_import.csv · CSV`
- `Рядків у файлі: 3. Товарів показано: 2.`
- Visible rows:
  - `Тестовий цукор`, `4820001112223`, `5.0 кг`
  - `Тестова кава`, `4820003334445`, `12.0 шт`
- The invalid row `Рядок без штрихк��ду` should not be visible in the item list.

## Reporting guidance

- If visual UI testing runs, record only the focused flow after setup is complete and annotate assertions.
- If Android runtime is unavailable, do not record an idle desktop. Attach command-output evidence and clearly mark visual assertions as untested.
