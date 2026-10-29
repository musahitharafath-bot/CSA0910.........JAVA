# Task 10 — Package structure proposal
```text
src/
  parking/model/Vehicle.java
  parking/model/ParkingSlot.java
  parking/model/ParkingTicket.java
  parking/service/ParkingService.java
  parking/service/PricingRule.java
  parking/app/Main.java
```
Keep fields private, expose validated public methods, use protected only for subclass extension, and use package-private access only when classes intentionally cooperate within the same package. Do not compare `Integer` wrappers with `==` when comparing numeric values; use `.equals()` or unbox safely.
