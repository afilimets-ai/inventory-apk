# Core Scan-to-Record Inventory Workflow

Implement the primary scan-driven workflow engine: scan barcode → look up item in Room → auto-advance to operation screen → confirm/adjust quantity → record operation → advance to next item. Each scan automatically progresses the workflow (auto-advance pattern). Manual touchscreen input only for exception handling (unknown barcode, quantity override). Workflow state persists across process death.

## Rationale
This is the core value proposition — the thing warehouse workers will do hundreds of times per shift. SAP requires excessive button clicks (pain-3-3), generic apps need manual navigation after each scan (pain-5-2). Scan-driven auto-advance with minimal touch interaction is the key differentiator that makes workers faster and happier. Directly addresses market gap-2.

## User Stories
- As a warehouse worker, I want each barcode scan to automatically show me the item details so that I can confirm and move to the next item without navigating menus
- As a warehouse worker, I want to complete an inventory operation in under 2 seconds per item so that I can process hundreds of items per shift efficiently
- As a warehouse worker, I want to manually enter a barcode when the physical label is damaged so that I can still record the item

## Acceptance Criteria
- [ ] Scanning a known barcode auto-navigates to the operation detail screen
- [ ] Item details (name, current quantity, location) displayed immediately after scan
- [ ] Quantity defaults to +1 for receiving, editable via large +/- buttons or keyboard
- [ ] Confirming an operation returns to scan-ready state within 1 second
- [ ] Unknown barcode triggers a 'new item' registration flow
- [ ] < 2 second total feedback loop: scan → display → ready for next scan
- [ ] Workflow state survives process death (SavedStateHandle)
- [ ] Manual barcode entry available as fallback for damaged barcodes
- [ ] Operation history visible with recent scans list
