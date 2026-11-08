import java.io.*;
import java.nio.file.*;

// Reproduces "Too many open files": f store files processed by V2 (try-with-resources) vs V3 (no close).
public class ScaleDemo {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("stores");
        int f = Integer.parseInt(args.length > 0 ? args[0] : "1000");
        String[] files = new String[f];
        for (int i = 0; i < f; i++) {
            Path p = dir.resolve("s" + i + ".csv"); Files.writeString(p, "pen,1,2.00\n"); files[i] = p.toString();
        }
        new File("output").mkdirs();
        TrackedReader.resetCounters();
        ReportGenerator.v2Twr(files, "output/scale_v2.txt");
        System.out.println("V2 try-with-resources : " + f + " files done, max handles open at once = " + TrackedReader.maxOpen
                + ", open at end = " + TrackedReader.open);
        TrackedReader.resetCounters();
        try { ReportGenerator.v3NoClose(files, "output/scale_v3.txt"); System.out.println("V3 no close: finished (limit not reached)"); }
        catch (IOException e) {
            System.out.println("V3 no close           : FAILED after " + TrackedReader.open + " handles -> " + e.getMessage());
        }
    }
}
