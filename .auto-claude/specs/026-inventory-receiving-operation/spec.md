# Inventory Receiving Operation

Implement the receiving workflow: select 'Receive' mode → scan item barcodes → auto-lookup in catalog → confirm/adjust quantity → record receipt with timestamp, operator, and location. Supports batch receiving (scan multiple items sequentially). Each receipt creates both an InventoryOperation record and an OutboxEntry in a single transaction. Running totals visible during the session.

## Rationale
Receiving goods is the most frequent inventory operation — every item entering the warehouse goes through receiving. This is the first real business operation that delivers user value on top of the technical foundation. Workers need to process deliveries quickly: scan, confirm, next.

## User Stories
- As a warehouse worker, I want to scan incoming items to record their receipt so that inventory counts are updated in real-time
- As a warehouse worker, I want to see a running total of items I've received so that I can verify against the delivery manifest

## Acceptance Criteria
- [ ] Receiving mode accessible from main menu with a single tap
- [ ] Scanning a barcode auto-populates item details from Room catalog
- [ ] Quantity defaults to 1, adjustable via +/- buttons or numeric input
- [ ] Each receive creates InventoryOperation + OutboxEntry in one @Transaction
- [ ] Running total shows items scanned in current session
- [ ] Session summary displayed when user ends the receiving session
- [ ] Unknown barcodes prompt 'Add new item' flow
- [ ] Location/zone can be set once per session (batch default)
