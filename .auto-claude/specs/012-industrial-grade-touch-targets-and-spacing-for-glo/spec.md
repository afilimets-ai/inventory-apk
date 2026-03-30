# Industrial-grade touch targets and spacing for glove-friendly operation

## Overview

Enforce minimum 60dp touch targets (80dp near screen edges) across all interactive Jetpack Compose components, with generous padding (16–24dp) between tap zones. The POC reference app uses wrap_content buttons and only 10dp padding — completely unsuitable for warehouse workers in gloves on the MT90 terminal's 5-inch display.

## Rationale

CLAUDE.md explicitly mandates 60dp minimum / 80dp near-edge touch targets for glove operation. The existing POC's 10dp padding and wrap_content button creates a 48dp-height Button that violates this requirement. Industrial terminals are operated one-handed while the other hand holds product — mis-taps waste time and cause frustration. This is the single most impactful UX requirement for the target hardware.

---
*This spec was created from ideation and is pending detailed specification.*
