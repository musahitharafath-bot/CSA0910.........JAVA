// Immutable result for ONE file (safe to publish to other threads without locking).
public record FileOutcome(String file, Status status, String studentId, double average, String grade, String message) {
    static FileOutcome ok(String f, String id, double avg, String grade) { return new FileOutcome(f, Status.OK, id, avg, grade, ""); }
    static FileOutcome fail(String f, Status s, String msg) { return new FileOutcome(f, s, "", 0, "", msg); }
}
