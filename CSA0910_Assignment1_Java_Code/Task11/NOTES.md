# Task 11 — Test checklist
| Test | Expected behaviour |
|---|---|
| Different vehicle types enter | Each gets a ticket and a free slot |
| Last free slot | Allocation succeeds |
| Facility full | Entry fails with a clear message |
| 0-minute stay | 0 billable hours and Rs 0 under the started-hour rule |
| 61-minute two-wheeler | 2 hours × Rs 10 = Rs 20 |
| Monthly-pass student | Rs 0 |
| Duplicate active registration | Rejected |
| Invalid exit registration | Rejected |
| Invalid floor/slot | Release returns false |

The included test program prints representative results. Run it and add your own observed results and screenshots to the report. Do not claim tests you did not run.
