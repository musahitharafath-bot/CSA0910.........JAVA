import java.time.LocalDateTime;

public class TicketDemo {
    public static void main(String[] args) {
        ParkingTicket t = new ParkingTicket(1000, "TN01A1", LocalDateTime.of(2026, 10, 26, 9, 0));
        System.out.println(t);
        try {
            new ParkingTicket(1001, "  ");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }
}
