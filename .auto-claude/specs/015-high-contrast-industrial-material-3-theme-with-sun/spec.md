# High-contrast industrial Material 3 theme with sunlight and low-light modes

## Overview

Design a Material 3 color scheme optimized for industrial environments — high contrast ratios (minimum 7:1 for body text, 4.5:1 for large text), distinct semantic colors for scan states, and automatic or manual switching between 'warehouse daylight' (high brightness, reduced color saturation) and 'standard' modes. Replace the POC's generic green/pink AppCompat theme.

## Rationale

The POC uses default AppCompat theme with colorPrimary=#008577 (teal green) and colorAccent=#D81B60 (pink) — colors chosen for aesthetics, not readability. Industrial terminals operate under fluorescent warehouse lighting, direct sunlight through loading dock doors, and dim storage areas. MT90's 5-inch screen at default brightness may be hard to read in bright sunlight. A purpose-built industrial theme ensures readability in all conditions and makes status colors (green=success, red=error, amber=warning) instantly recognizable. This also establishes the theming foundation for white-label product flavors.

---
*This spec was created from ideation and is pending detailed specification.*
