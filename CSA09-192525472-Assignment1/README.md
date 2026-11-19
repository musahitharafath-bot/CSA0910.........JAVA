# CSA09-192525472-Assignment1

**Design, Implement and Validate an Object-Oriented Model for a Campus Smart-Parking System**

Student: Syed Aakif Hussain S A  |  Register Number: 192525472  |  Course: Java Programming (CO1)  |  SIMATS Engineering

Compiled and run with Java 21.0.12.1

## Folder layout

Each task has its own folder with the same three sub-folders:

```
TaskNN/
  src/       Java source code
  input/     input files (where the task uses any)
  output/    program output (.txt) and console screenshots (.png)
  README.md  what the task contains and how to run it
  run.sh     compile and run commands
```

| Folder | Task | Main source files |
|---|---|---|
| [Task01](Task01/) | Understand the Problem and Identify the Objects | Task01Demo.java |
| [Task02](Task02/) | Choose the Data Representation | DataTypesDemo.java, InputDemo.java, JaggedArrayDemo.java |
| [Task03](Task03/) | Design the Control Logic | ParkingFacility.java |
| [Task04](Task04/) | Design Classes, Objects and Constructors | ParkingSlot.java, ParkingTicket.java, SlotDemo.java, TicketDemo.java |
| [Task05](Task05/) | Apply static Members and Object Life Cycle | TicketSequence.java |
| [Task06](Task06/) | Design the Inheritance Hierarchy | ConstructorTrace.java |
| [Task07](Task07/) | Override and Specialise Behaviour | OverrideDemo.java |
| [Task08](Task08/) | Apply Polymorphism | FeeDemo.java, OverloadDemo.java |
| [Task09](Task09/) | Introduce Abstraction | AbstractDemo.java, ChargeableDemo.java |
| [Task10](Task10/) | Enforce Encapsulation through Packages and Access Modifiers | FacilityConfig.java, WrapperDemo.java, Main.java, Car.java, Chargeable.java, ElectricCar.j |
| [Task11](Task11/) | Implement, Test and Defend the Design | DailyCollection.java, FeeDemo.java, ParkingFacility.java, ParkingTests.java |
| [Task12](Task12/) | Re-evaluate and Adapt the Design | Task12.java, Task12Demo.java, VanCases.java |

## Running

Requires JDK 17 or later (pattern matching for `instanceof` is used).

```
cd Task08
sh run.sh
```

Task 10 is the only multi-package project (`parking.model`, `parking.service`, `parking.app`); its `run.sh` compiles all packages and runs `parking.app.Main` with `input/events.txt`.

## Suggested commit history

One commit per task, for example: `Task01: problem analysis demo`, `Task02: data representation`, … `Task12: strategy-based redesign`.
