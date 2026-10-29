import java.util.ArrayList;
import java.util.List;

/** Task 5: static ticket counter and object life cycle. */
public class Task05Demo {
    static class ParkingTicket {
        private static long nextTicketNumber = 1;
        private final long ticketNumber;
        private final String vehicleNumber;

        ParkingTicket(String vehicleNumber) {
            if (vehicleNumber == null || vehicleNumber.isBlank())
                throw new IllegalArgumentException("Vehicle number required");
            this.vehicleNumber = vehicleNumber;
            this.ticketNumber = nextTicketNumber++;
        }

        long getTicketNumber() { return ticketNumber; }
        String getVehicleNumber() { return vehicleNumber; }
        @Override public String toString() {
            return "Ticket{" + ticketNumber + ", vehicle='" + vehicleNumber + "'}";
        }
    }

    public static void main(String[] args) {
        List<ParkingTicket> activeTickets = new ArrayList<>();
        activeTickets.add(new ParkingTicket("TN01AB1111"));
        activeTickets.add(new ParkingTicket("TN02CD2222"));
        System.out.println(activeTickets);

        ParkingTicket leaving = activeTickets.remove(0);
        System.out.println("Vehicle exited with " + leaving);
        leaving = null; // If no references remain, this object can be garbage-collected.
        System.out.println("Remaining active tickets: " + activeTickets);
        System.out.println("Next ticket: " + new ParkingTicket("TN03EF3333"));
    }
}
