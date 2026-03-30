# Scan-in-progress state indicator with visual scanner-active animation

## Overview

Show a clear visual indicator when the barcode scanner is active and awaiting a scan, including a pulsing animation, countdown timer (matching SCAN_TIMEOUT), and instruction text. The POC fires the scan intent but provides zero feedback that the scanner is active.

## Rationale

The POC sends the nlscan.action.SCANNER_TRIG broadcast (with SCAN_TIMEOUT=4 seconds) but provides no visual feedback that the scanner laser is active. Users press the hardware button or tap 'Scan' and see nothing change — they don't know if the scanner activated, if it's still waiting, or if the timeout expired. This creates a pattern of repeated button-pressing and frustration, especially for new users unfamiliar with the MT90 hardware.

---
*This spec was created from ideation and is pending detailed specification.*
