import java.io.*;

public class TrackedWriter extends BufferedWriter {
    private final String name; private boolean closed = false;
    public TrackedWriter(String name) throws IOException {
        super(new FileWriter(name));
        this.name = name; TrackedReader.open++; TrackedReader.maxOpen = Math.max(TrackedReader.maxOpen, TrackedReader.open);
        TrackedReader.openNames.add(name);
    }
    @Override public void close() throws IOException {
        super.close();
        if (!closed) { closed = true; TrackedReader.open--; TrackedReader.openNames.remove(name); }
    }
}
