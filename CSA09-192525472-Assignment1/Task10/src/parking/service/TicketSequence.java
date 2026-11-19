package parking.service;

public final class TicketSequence {
    private static long nextId = 1000;
    private TicketSequence() { }
    public static synchronized long nextTicketId() { return nextId++; }
}
