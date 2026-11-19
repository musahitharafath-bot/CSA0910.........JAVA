public class ParkingTicket {
    private final long ticketId;
    private final String regNo;
    private final java.time.LocalDateTime entryTime;

    ParkingTicket(long ticketId, String regNo) {
        this(ticketId, regNo, java.time.LocalDateTime.now());
    }

    ParkingTicket(long ticketId, String regNo, java.time.LocalDateTime entryTime) {
        if (regNo == null || regNo.isBlank())
            throw new IllegalArgumentException("Registration required");
        this.ticketId = ticketId;
        this.regNo = regNo;
        this.entryTime = java.util.Objects.requireNonNull(entryTime);
    }

    public long getTicketId() { return ticketId; }
    public String getRegNo() { return regNo; }
    public java.time.LocalDateTime getEntryTime() { return entryTime; }

    @Override
    public String toString() {
        return "Ticket#" + ticketId + "[" + regNo + " @ " + entryTime + "]";
    }
}
