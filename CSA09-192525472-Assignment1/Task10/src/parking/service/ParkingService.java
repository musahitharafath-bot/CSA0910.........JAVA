package parking.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import parking.model.Chargeable;
import parking.model.ParkingTicket;
import parking.model.Vehicle;

public class ParkingService {
    private final ParkingFacility facility;
    private final Map<String, ParkingTicket> active = new HashMap<>();

    public ParkingService(ParkingFacility facility) { this.facility = facility; }

    public String enter(Vehicle v, LocalDateTime time) {
        if (active.containsKey(v.getRegNo()))
            return "REJECTED " + v + ": already has an active ticket";
        int[] pos = facility.allocate(v.getRegNo());
        if (pos == null)
            return "REJECTED " + v + ": no capacity";
        ParkingTicket t = new ParkingTicket(TicketSequence.nextTicketId(), v, time, pos[0], pos[1]);
        active.put(v.getRegNo(), t);
        return "ENTER    " + v + " -> ticket " + t.getTicketId()
                + ", floor " + pos[0] + ", slot " + pos[1];
    }

    public String exit(String regNo, LocalDateTime time, int kWh) {
        ParkingTicket t = active.get(regNo.trim().toUpperCase());
        if (t == null)
            return "REJECTED " + regNo + ": unknown or closed ticket";
        int minutes = (int) Duration.between(t.getEntryTime(), time).toMinutes();
        Vehicle v = t.getVehicle();
        double parking = v.calculateFee(minutes);
        double charging = (v instanceof Chargeable c) ? c.chargingFee(kWh) : 0.0;
        facility.release(t.getFloor(), t.getSlot());
        active.remove(v.getRegNo());
        return String.format("EXIT     %s ticket %d: %d min, parking Rs %.1f + charging Rs %.1f = Rs %.1f",
                v, t.getTicketId(), minutes, parking, charging, parking + charging);
    }
}
