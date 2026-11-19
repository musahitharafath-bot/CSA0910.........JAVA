import java.io.*;
import java.util.*;

// A BufferedReader that counts how many instances are currently open (our "file handle" meter).
public class TrackedReader extends BufferedReader {
    static int open = 0, maxOpen = 0;
    static final List<String> openNames = new ArrayList<>();
    private final String name; private boolean closed = false;
    public TrackedReader(String name) throws FileNotFoundException {
        super(new FileReader(name));
        this.name = name; open++; maxOpen = Math.max(maxOpen, open); openNames.add(name);
    }
    @Override public void close() throws IOException {
        super.close();
        if (!closed) { closed = true; open--; openNames.remove(name); }
    }
    static void resetCounters() { open = 0; maxOpen = 0; openNames.clear(); }
}
