# Task 6 — UML sketch
```text
Vehicle
  ^
  +-- Car
        ^
        +-- ElectricCar

Vehicle
  +-- TwoWheeler
```
This is hierarchical inheritance, with a multilevel path Vehicle → Car → ElectricCar. Constructor trace: Vehicle constructor, then Car constructor, then ElectricCar constructor.
