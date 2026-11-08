import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

// One Callable per file. EXPECTED failures are turned into a FileOutcome (never thrown);
// anything UNEXPECTED (a bug) escapes call() and is captured by the Future.
public class ScoreTask implements Callable<FileOutcome> {
    private final String name; private final Path path; private final long latencyMs;
    private final ConcurrentHashMap<String, FileOutcome> results;
    private final ConcurrentHashMap<Status, LongAdder> counts;

    ScoreTask(String name, Path path, long latencyMs, ConcurrentHashMap<String, FileOutcome> results, ConcurrentHashMap<Status, LongAdder> counts) {
        this.name = name; this.path = path; this.latencyMs = latencyMs; this.results = results; this.counts = counts;
    }
    static void record(ConcurrentHashMap<String, FileOutcome> results, ConcurrentHashMap<Status, LongAdder> counts, FileOutcome o) {
        if (results.putIfAbsent(o.file(), o) == null) counts.computeIfAbsent(o.status(), s -> new LongAdder()).increment();
    }

    @Override public FileOutcome call() throws Exception {
        if (latencyMs > 0) Thread.sleep(latencyMs);                     // simulated slow storage
        if (name.contains("poison")) throw new IllegalStateException("simulated bug while processing " + name);
        FileOutcome o;
        try { o = ScoreParser.parse(name, path); }
        catch (NoSuchFileException e)          { o = FileOutcome.fail(name, Status.MISSING, "file not found"); }
        catch (IOException e)                  { o = FileOutcome.fail(name, Status.IO_ERROR, e.getClass().getSimpleName() + ": " + e.getMessage()); }
        catch (ScoreFormatException e)         { o = FileOutcome.fail(name, Status.CORRUPT, e.getMessage()); }
        catch (ScoreValidationException e)     { o = FileOutcome.fail(name, Status.INVALID, e.getMessage()); }
        record(results, counts, o);
        return o;
    }
}
