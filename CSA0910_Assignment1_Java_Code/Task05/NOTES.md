# Task 5 — static and lifecycle
The ticket counter belongs to the class because all ticket objects share a single sequence. A per-facility counter is appropriate only if ticket numbers need to be unique within each facility rather than globally. Removing a ticket from active storage and dropping all other references makes it eligible for garbage collection; collection timing is JVM-controlled.
