# Industrial-Grade Compose UI Design System

Build a Jetpack Compose design system optimized for industrial handheld use: minimum 60dp touch targets (80dp near screen edges) for gloved operation, high-contrast Material 3 theme with sunlight-readable colors, large typography scale, scan-centric layout templates (status bar + main content + action area), and consistent component library (buttons, cards, input fields, lists).

## Rationale
Every competitor fails at industrial mobile UX — SAP requires pinch-zoom on handhelds (pain-3-1), RFgen has outdated interfaces (pain-6-1), and generic apps use standard 48dp touch targets unusable with gloves. This is our highest-impact differentiator (market gap-2). Workers wearing gloves in cold storage need oversized, high-contrast UI elements.

## User Stories
- As a warehouse worker wearing gloves, I want large tap targets so that I can accurately press buttons without removing my gloves
- As a warehouse worker, I want high-contrast text and colors so that I can read the screen in bright sunlight or dim warehouse conditions

## Acceptance Criteria
- [ ] All interactive elements meet minimum 60dp touch target size
- [ ] Elements near screen edges (< 40dp from edge) are at least 80dp
- [ ] High-contrast color palette readable in direct sunlight and dim warehouse lighting
- [ ] Typography scale uses minimum 16sp body text, 20sp+ for scan results
- [ ] Consistent component library: IndustrialButton, ScanResultCard, StatusBar, OperationList
- [ ] Layout templates follow scan-centric pattern: status → content → action
- [ ] Usability validated with gloves on physical MT90 device
- [ ] Supports both light and dark themes for varied warehouse lighting
