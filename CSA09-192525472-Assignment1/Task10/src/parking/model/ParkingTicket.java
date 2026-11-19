package parking.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class ParkingTicket {
    private final long ticketId;
    private final Vehicle vehicle;
    private final LocalDateTime entryTime;
    private final int floor;
    private final int slot;

    public ParkingTicket(long ticketId, Vehicle vehicle, LocalDateTime entryTime,
                         int floor, int slot) {
        this.ticketId = ticketId;
        this.vehicle = Objects.requireNonNull(vehicle);
        this.entryTime = Objects.requireNonNull(entryTime);
        this.floor = floor;
        this.slot = slot;
    }

    public long getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public int getFloor() { return floor; }
    public int getSlot() { return slot; }
}
