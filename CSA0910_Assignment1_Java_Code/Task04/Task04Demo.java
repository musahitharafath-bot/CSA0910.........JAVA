import java.time.LocalDateTime;

/** Task 4: constructors, constructor chaining and object references. */
public class Task04Demo {
    static class ParkingTicket {
        private final long ticketNumber;
        private final String registrationNumber;
        private final LocalDateTime entryTime;

        ParkingTicket() {
            this(0L, "UNKNOWN", LocalDateTime.now());
        }

        ParkingTicket(long ticketNumber, String registrationNumber) {
            this(ticketNumber, registrationNumber, LocalDateTime.now());
        }

        ParkingTicket(long ticketNumber, String registrationNumber, LocalDateTime entryTime) {
            if (ticketNumber < 0) throw new IllegalArgumentException("Ticket number cannot be negative");
            if (registrationNumber == null || registrationNumber.isBlank())
                throw new IllegalArgumentException("Registration number required");
            if (entryTime == null) throw new IllegalArgumentException("Entry time required");
            this.ticketNumber = ticketNumber;
            this.registrationNumber = registrationNumber;
            this.entryTime = entryTime;
        }

        ParkingTicket(ParkingTicket other) {
            this(other.ticketNumber, other.registrationNumber, other.entryTime);
        }

        @Override public String toString() {
            return "Ticket #" + ticketNumber + " for " + registrationNumber + " at " + entryTime;
        }
    }

    public static void main(String[] args) {
        ParkingTicket first = new ParkingTicket(1001, "TN01AB1111");
        ParkingTicket copied = new ParkingTicket(first);
        System.out.println(first);
        System.out.println("Copy: " + copied);
        System.out.println("Different objects: " + (first != copied));
    }
}
