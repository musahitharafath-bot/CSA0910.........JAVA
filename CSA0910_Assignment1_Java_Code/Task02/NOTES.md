# Task 2 — Representation
- Use `int` for floor/slot indices and duration minutes, `String` for registration number, `long` for ticket ID/time counters, and `double` or `BigDecimal` for money.
- Use a jagged `String[][]` because each floor may have a different number of slots.
- `75 / 60` with integer arithmetic equals 1; billable hours must round up using quotient plus a remainder check.
- A single flat array is convenient only when the number of slots per floor is uniform or a mapping table is maintained.
