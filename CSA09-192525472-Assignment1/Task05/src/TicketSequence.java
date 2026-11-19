public class TicketSequence {
    private static final long START = 1000;
    private static long nextId;

    static {                       // runs once, when the class is loaded
        nextId = START;
    }

    private TicketSequence() { }   // utility class: no instances

    public static synchronized long nextTicketId() {
        return nextId++;
    }

    public static void main(String[] args) {
        System.out.println(nextTicketId() + " " + nextTicketId()
                + " " + nextTicketId());
    }
}
