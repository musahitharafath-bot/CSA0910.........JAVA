// CHECKED: the file exists but its content is not a valid score record (expected, handled per file).
public class ScoreFormatException extends Exception {
    public ScoreFormatException(String m) { super(m); }
    public ScoreFormatException(String m, Throwable c) { super(m, c); }
}
