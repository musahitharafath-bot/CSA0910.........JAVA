import java.util.Objects;

/** Task 7: overriding and Object methods. */
public class Task07Demo {
    static abstract class Vehicle {
        private final String regNo;
        Vehicle(String regNo) {
            if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("Registration required");
            this.regNo = regNo;
        }
        public final String getRegNo() { return regNo; }
        public abstract double calculateFee(int minutes);

        @Override public final boolean equals(Object obj) {
            return obj instanceof Vehicle other && regNo.equals(other.regNo);
        }
        @Override public final int hashCode() { return Objects.hash(regNo); }
        @Override public String toString() { return getClass().getSimpleName() + "[" + regNo + "]"; }
    }
    static final class Car extends Vehicle {
        Car(String regNo) { super(regNo); }
        @Override public double calculateFee(int minutes) {
            if (minutes < 0) throw new IllegalArgumentException("Negative duration");
            int hours = minutes / 60 + (minutes % 60 == 0 ? 0 : 1);
            return hours * 30.0;
        }
    }

    public static void main(String[] args) {
        Car a = new Car("TN02B2");
        Car b = new Car("TN02B2");
        System.out.println(a + " fee for 61 minutes = Rs " + a.calculateFee(61));
        System.out.println("Equal registrations: " + a.equals(b));
        System.out.println("Equal hash codes: " + (a.hashCode() == b.hashCode()));
    }
}
