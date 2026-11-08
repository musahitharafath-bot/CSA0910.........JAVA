// CHECKED: the record is well-formed but a mark is outside 0..100.
public class ScoreValidationException extends Exception {
    public ScoreValidationException(String m) { super(m); }
}
