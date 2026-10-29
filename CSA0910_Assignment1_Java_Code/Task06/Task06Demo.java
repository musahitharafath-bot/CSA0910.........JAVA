/** Task 6: hierarchical inheritance and constructor execution order. */
public class Task06Demo {
    static class Vehicle {
        protected final String registrationNumber;
        Vehicle(String registrationNumber) {
            this.registrationNumber = registrationNumber;
            System.out.println("Vehicle constructor");
        }
    }
    static class Car extends Vehicle {
        protected final int seats;
        Car(String registrationNumber, int seats) {
            super(registrationNumber);
            this.seats = seats;
            System.out.println("Car constructor");
        }
    }
    static class ElectricCar extends Car {
        private final int batteryKWh;
        ElectricCar(String registrationNumber, int seats, int batteryKWh) {
            super(registrationNumber, seats);
            this.batteryKWh = batteryKWh;
            System.out.println("ElectricCar constructor");
        }
        void display() {
            System.out.println(registrationNumber + ", seats=" + seats + ", battery=" + batteryKWh + " kWh");
        }
    }

    public static void main(String[] args) {
        ElectricCar car = new ElectricCar("TN03C3", 5, 60);
        car.display();
    }
}
