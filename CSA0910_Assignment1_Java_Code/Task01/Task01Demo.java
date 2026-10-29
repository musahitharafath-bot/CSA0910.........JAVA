import java.util.*;

/**
 * Task 1: Candidate objects, responsibilities, inputs and outputs.
 * Demonstration of a small object-oriented model.
 */
public class Task01Demo {
    enum VehicleType { TWO_WHEELER, CAR, ELECTRIC_CAR }

    static final class Vehicle {
        private final String registrationNumber;
        private final VehicleType type;

        Vehicle(String registrationNumber, VehicleType type) {
            if (registrationNumber == null || registrationNumber.isBlank())
                throw new IllegalArgumentException("Registration number is required");
            this.registrationNumber = registrationNumber;
            this.type = Objects.requireNonNull(type);
        }

        String getRegistrationNumber() { return registrationNumber; }
        VehicleType getType() { return type; }
    }

    static final class ParkingSlot {
        private final int floor;
        private final int slotNumber;
        private Vehicle vehicle;

        ParkingSlot(int floor, int slotNumber) {
            if (floor < 0 || slotNumber < 0) throw new IllegalArgumentException("Negative location");
            this.floor = floor;
            this.slotNumber = slotNumber;
        }

        boolean isFree() { return vehicle == null; }
        boolean park(Vehicle candidate) {
            if (!isFree()) return false;
            vehicle = Objects.requireNonNull(candidate);
            return true;
        }
        Vehicle release() {
            Vehicle old = vehicle;
            vehicle = null;
            return old;
        }
        @Override public String toString() {
            return "Floor " + floor + ", slot " + slotNumber + ", free=" + isFree();
        }
    }

    public static void main(String[] args) {
        Vehicle v = new Vehicle("TN01AB1111", VehicleType.CAR);
        ParkingSlot slot = new ParkingSlot(0, 1);
        System.out.println("Vehicle: " + v.getRegistrationNumber() + " (" + v.getType() + ")");
        System.out.println("Park successful: " + slot.park(v));
        System.out.println(slot);
        System.out.println("Second vehicle can use occupied slot: " +
                slot.park(new Vehicle("TN02CD2222", VehicleType.TWO_WHEELER)));
        System.out.println("Released: " + slot.release().getRegistrationNumber());
        System.out.println("Slot free after release: " + slot.isFree());
    }
}
