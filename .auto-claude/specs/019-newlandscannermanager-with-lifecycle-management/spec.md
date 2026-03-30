# NewlandScannerManager with Lifecycle Management

Implement the NewlandScannerManager as an Application-scoped singleton: register/unregister BroadcastReceiver for nlscan.action.SCANNER_RESULT, handle hardware button (KeyEvent.KEYCODE_F6), programmatic trigger via nlscan.action.SCANNER_TRIG, debounce(300ms) protection against double-scans, and scanEvents: SharedFlow<ScanResult> for reactive consumption by ViewModels.

## Rationale
The MT90's built-in barcode scanner is the primary input device — not the touchscreen. The existing POC registers a BroadcastReceiver but never unregisters it (memory leak). Competitors like Honeywell suffer from scanner software conflicts (pain-2-1) and Zebra's DataWedge requires complex middleware configuration (pain-1-2). Our direct broadcast integration is simpler and more reliable.

## User Stories
- As a warehouse worker, I want to press the hardware scan button and have the barcode instantly captured so that I can scan items without touching the screen
- As a developer, I want a reactive scanner API (SharedFlow) so that any screen can observe scan results without tight coupling to the scanner lifecycle

## Acceptance Criteria
- [ ] NewlandScannerManager is singleton in Application scope (Hilt @Singleton)
- [ ] BroadcastReceiver registers in register() and unregisters in unregister()
- [ ] Hardware scan button (F6) triggers barcode scan
- [ ] Programmatic trigger via trigger(timeoutSec) method works
- [ ] scanEvents SharedFlow emits ScanResult(barcode, barcodeType) to collectors
- [ ] 300ms debounce prevents duplicate scan events
- [ ] No memory leak — receiver properly cleaned up on destroy
- [ ] Validated on physical MT90 device (not emulator)
