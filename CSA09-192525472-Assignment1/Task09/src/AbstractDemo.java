abstract class Vehicle {
    private final String regNo;
    protected Vehicle(String regNo) { this.regNo = regNo; }
    public String getRegNo() { return regNo; }
    protected abstract double ratePerHour();
}

class Car extends Vehicle {
    Car(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return 30; }
}

public class AbstractDemo {
    public static void main(String[] args) {
        // Vehicle v = new Vehicle("X");   // compile error: Vehicle is abstract
        Vehicle v = new Car("TN02B2");
        System.out.println(v.getRegNo() + " rate = " + v.ratePerHour());
    }
}
