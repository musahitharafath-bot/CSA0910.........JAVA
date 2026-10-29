import java.time.LocalTime;
import java.util.*;

/**
 * Task 12: modified design supporting e-bikes, delivery vans, jagged floors,
 * and one-day passes for visitors.
 */
public class Task12ExtendedParkingSystem {
    interface Pass {
        boolean isValid();
        double discountFor(double fee);
    }

    static class OneDayPass implements Pass {
        private final boolean valid;
        OneDayPass(boolean valid) { this.valid = valid; }
        @Override public boolean isValid() { return valid; }
        @Override public double discountFor(double fee) { return valid ? fee * 0.50 : fee; }
    }

    enum VehicleType { TWO_WHEELER, CAR, ELECTRIC_CAR, E_BIKE, DELIVERY_VAN }
    static class Vehicle {
        final String regNo;
        final VehicleType type;
        final Pass pass;
        Vehicle(String regNo, VehicleType type, Pass pass) {
            if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("Registration required");
            this.regNo = regNo;
            this.type = Objects.requireNonNull(type);
            this.pass = pass;
        }

        double calculateFee(int minutes, LocalTime entryTime) {
            if (minutes < 0) throw new IllegalArgumentException("Negative duration");
            int hours = minutes / 60 + (minutes % 60 == 0 ? 0 : 1);
            double rate = switch (type) {
                case TWO_WHEELER -> 10.0;
                case CAR, ELECTRIC_CAR -> 30.0;
                case E_BIKE -> 15.0; // Flat parking fee, independent of duration
                case DELIVERY_VAN -> 30.0;
            };
            double fee;
            if (type == VehicleType.E_BIKE) {
                fee = 25.0; // flat fee per visit
            } else if (type == VehicleType.DELIVERY_VAN &&
                    !entryTime.isBefore(LocalTime.of(9, 0)) &&
                    entryTime.isBefore(LocalTime.of(17, 0))) {
                fee = hours * rate * 2.0;
            } else {
                fee = hours * rate;
            }
            if (pass != null && pass.isValid()) fee = pass.discountFor(fee);
            return fee;
        }
    }

    static class ParkingFacility {
        private final String[][] slots; // jagged array supports different capacity per floor
        ParkingFacility(int[] slotsPerFloor) {
            slots = new String[slotsPerFloor.length][];
            for (int f = 0; f < slotsPerFloor.length; f++) {
                if (slotsPerFloor[f] < 0) throw new IllegalArgumentException("Negative slot count");
                slots[f] = new String[slotsPerFloor[f]];
            }
        }
        int[] allocate(String regNo) {
            for (int f = 0; f < slots.length; f++) {
                for (int s = 0; s < slots[f].length; s++) {
                    if (slots[f][s] == null) {
                        slots[f][s] = regNo;
                        return new int[]{f, s};
                    }
                }
            }
            return null;
        }
        boolean release(int floor, int slot) {
            if (floor < 0 || floor >= slots.length || slot < 0 || slot >= slots[floor].length
                    || slots[floor][slot] == null) return false;
            slots[floor][slot] = null;
            return true;
        }
    }

    public static void main(String[] args) {
        ParkingFacility facility = new ParkingFacility(new int[]{2, 3, 1, 4, 2});
        System.out.println("E-bike location: " + Arrays.toString(facility.allocate("TN10EB1000")));
        System.out.println("Van location: " + Arrays.toString(facility.allocate("TN20DV2000")));

        Vehicle eBike = new Vehicle("TN10EB1000", VehicleType.E_BIKE, null);
        Vehicle vanPeak = new Vehicle("TN20DV2000", VehicleType.DELIVERY_VAN, null);
        Vehicle visitorWithPass = new Vehicle("TN30CA3000", VehicleType.CAR, new OneDayPass(true));

        System.out.println("E-bike flat fee: Rs " + eBike.calculateFee(180, LocalTime.of(10, 0)));
        System.out.println("Delivery van peak fee: Rs " + vanPeak.calculateFee(120, LocalTime.of(10, 0)));
        System.out.println("Visitor car fee with one-day pass: Rs " +
                visitorWithPass.calculateFee(120, LocalTime.of(18, 0)));
        System.out.println("Invalid slot release: " + facility.release(99, 99));
    }
}
