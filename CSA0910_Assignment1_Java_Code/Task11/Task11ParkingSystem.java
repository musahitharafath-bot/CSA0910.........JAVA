import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/** Task 11: integrated parking system and small validation suite. */
public class Task11ParkingSystem {
    enum VehicleType { TWO_WHEELER, CAR, ELECTRIC_CAR }
    enum UserType { STUDENT, STAFF, VISITOR }

    static class Vehicle {
        final String regNo;
        final VehicleType type;
        final UserType userType;
        final boolean monthlyPass;

        Vehicle(String regNo, VehicleType type, UserType userType, boolean monthlyPass) {
            if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("Registration required");
            this.regNo = regNo;
            this.type = Objects.requireNonNull(type);
            this.userType = Objects.requireNonNull(userType);
            this.monthlyPass = monthlyPass;
        }

        double ratePerHour() {
            return switch (type) {
                case TWO_WHEELER -> 10.0;
                case CAR -> 30.0;
                case ELECTRIC_CAR -> 30.0;
            };
        }

        double fee(long minutes) {
            if (minutes < 0) throw new IllegalArgumentException("Duration cannot be negative");
            if (monthlyPass && (userType == UserType.STUDENT || userType == UserType.STAFF)) return 0.0;
            long hours = minutes / 60 + (minutes % 60 == 0 ? 0 : 1);
            return hours * ratePerHour();
        }
    }

    static class Ticket {
        private static long nextId = 1;
        final long id = nextId++;
        final Vehicle vehicle;
        final int floor;
        final int slot;
        final LocalDateTime entry = LocalDateTime.now();

        Ticket(Vehicle vehicle, int floor, int slot) {
            this.vehicle = vehicle;
            this.floor = floor;
            this.slot = slot;
        }
    }

    static class ParkingSystem {
        private final String[][] slots;
        private final Map<String, Ticket> active = new LinkedHashMap<>();
        private double totalCollection = 0;

        ParkingSystem(int[] slotsPerFloor) {
            slots = new String[slotsPerFloor.length][];
            for (int i = 0; i < slotsPerFloor.length; i++) {
                if (slotsPerFloor[i] < 0) throw new IllegalArgumentException("Negative slots");
                slots[i] = new String[slotsPerFloor[i]];
            }
        }

        Ticket enter(Vehicle vehicle) {
            if (active.containsKey(vehicle.regNo)) throw new IllegalArgumentException("Duplicate registration");
            for (int f = 0; f < slots.length; f++) {
                for (int s = 0; s < slots[f].length; s++) {
                    if (slots[f][s] == null) {
                        slots[f][s] = vehicle.regNo;
                        Ticket ticket = new Ticket(vehicle, f, s);
                        active.put(vehicle.regNo, ticket);
                        return ticket;
                    }
                }
            }
            throw new IllegalStateException("Parking full");
        }

        double exit(String regNo, long minutes) {
            Ticket ticket = active.remove(regNo);
            if (ticket == null) throw new IllegalArgumentException("No active ticket for " + regNo);
            slots[ticket.floor][ticket.slot] = null;
            double fee = ticket.vehicle.fee(minutes);
            totalCollection += fee;
            return fee;
        }

        double getTotalCollection() { return totalCollection; }
        int activeCount() { return active.size(); }
    }

    public static void main(String[] args) {
        ParkingSystem parking = new ParkingSystem(new int[]{1, 1});
        Ticket t1 = parking.enter(new Vehicle("TN01AB1111", VehicleType.TWO_WHEELER, UserType.VISITOR, false));
        Ticket t2 = parking.enter(new Vehicle("TN02CD2222", VehicleType.CAR, UserType.STUDENT, true));
        System.out.println("Ticket issued: " + t1.id + " at (" + t1.floor + "," + t1.slot + ")");
        System.out.println("Ticket issued: " + t2.id + " at (" + t2.floor + "," + t2.slot + ")");
        System.out.println("Two-wheeler fee for 61 minutes: Rs " + parking.exit("TN01AB1111", 61));
        System.out.println("Student monthly-pass fee for 120 minutes: Rs " + parking.exit("TN02CD2222", 120));
        System.out.println("Total collection: Rs " + parking.getTotalCollection());
        System.out.println("Active vehicles: " + parking.activeCount());

        try {
            parking.exit("UNKNOWN", 30);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid exit handled: " + e.getMessage());
        }
    }
}
