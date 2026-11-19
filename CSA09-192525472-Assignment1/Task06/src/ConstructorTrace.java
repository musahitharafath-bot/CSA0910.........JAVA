class Vehicle {
    private final String regNo;
    Vehicle(String regNo) {
        this.regNo = regNo;
        System.out.println("1. Vehicle constructor body finished (regNo = " + regNo + ")");
    }
}

class Car extends Vehicle {
    Car(String regNo) {
        super(regNo);
        System.out.println("2. Car constructor body finished");
    }
}

class ElectricCar extends Car {
    ElectricCar(String regNo) {
        super(regNo);
        System.out.println("3. ElectricCar constructor body finished");
    }
}

public class ConstructorTrace {
    public static void main(String[] args) {
        new ElectricCar("TN03C3");
    }
}
