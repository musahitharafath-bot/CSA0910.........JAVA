# Task 3 — Pseudocode
## Allocate
1. For each floor from first to last, inspect each slot.
2. If a slot is null, store the vehicle number and return its floor and slot indices.
3. If no slot is free, return null.
## Release
1. Validate floor bounds.
2. Validate slot bounds for that floor.
3. If already null, return false; otherwise set null and return true.
## Fee
1. Reject negative minutes.
2. Compute hours = minutes / 60 + (minutes % 60 == 0 ? 0 : 1).
3. Fee = hours × rate, unless a valid pass changes the fee.
