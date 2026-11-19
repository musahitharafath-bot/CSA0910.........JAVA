import java.util.HashSet;
import java.util.Set;

abstract class Vehicle {
    private final String regNo;
    Vehicle(String regNo) { this.regNo = regNo; }
    protected abstract double ratePerHour();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + regNo + "]";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Vehicle && regNo.equals(((Vehicle) other).regNo);
    }

    @Override
    public int hashCode() { return regNo.hashCode(); }
}

class Car extends Vehicle {
    Car(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return 30.0; }
}

class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return 10.0; }
}

public class OverrideDemo {
    public static void main(String[] args) {
        Vehicle a = new Car("TN02B2");
        Vehicle b = new Car("TN02B2");
        System.out.println(a);
        System.out.println("a.equals(b) = " + a.equals(b));
        System.out.println("a == b      = " + (a == b));
        Set<Vehicle> active = new HashSet<>();
        active.add(a);
        active.add(b);
        System.out.println("HashSet size = " + active.size());
        System.out.println("Rates: " + new TwoWheeler("TN01A1").ratePerHour() + " / " + a.ratePerHour());
    }
}
