import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Q4: the two evening reports produced from HashMap, TreeMap and LinkedHashMap (data: input/medicines.csv). */
public class PharmacyDemo {
    record Medicine(String code, String name, int qty) { }

    static String report(Collection<Medicine> c) {
        StringBuilder sb = new StringBuilder();
        for (Medicine m : c) sb.append(m.code()).append(' ').append(m.qty()).append(", ");
        return sb.substring(0, sb.length() - 2);
    }

    public static void main(String[] args) throws Exception {
        List<Medicine> added = new ArrayList<>();
        for (String line : Files.readAllLines(Path.of("input/medicines.csv")).subList(1, 13)) {
            String[] p = line.split(",");
            added.add(new Medicine(p[0], p[1], Integer.parseInt(p[2])));
        }
        Map<String, Medicine> hash = new HashMap<>(), tree = new TreeMap<>(), linked = new LinkedHashMap<>();
        for (Medicine m : added) { hash.put(m.code(), m); tree.put(m.code(), m); linked.put(m.code(), m); }

        List<Medicine> codeOrder = new ArrayList<>(added);
        codeOrder.sort(Comparator.comparing(Medicine::code));

        System.out.println("Lookup MED0762 -> " + hash.get("MED0762").name() + " / " + tree.get("MED0762").name() + " / " + linked.get("MED0762").name());
        System.out.println();
        String wantCode = report(codeOrder), wantAdded = report(added);
        System.out.println("REQUIRED code order : " + wantCode);
        System.out.println("REQUIRED added order: " + wantAdded);
        System.out.println();
        for (var e : Map.of("HashMap", hash, "TreeMap", tree, "LinkedHashMap", linked).entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            String r = report(e.getValue().values());
            System.out.printf("%-14s natural iteration: code order? %-5s  added order? %-5s%n", e.getKey(), r.equals(wantCode), r.equals(wantAdded));
        }
        // extra work needed when the 'wrong' map is used
        List<Medicine> fix = new ArrayList<>(hash.values());
        fix.sort(Comparator.comparing(Medicine::code));
        System.out.println();
        System.out.println("HashMap + sort keys  -> code order correct? " + report(fix).equals(wantCode) + "  (extra O(n log n) sort)");
        System.out.println("HashMap added order  -> cannot be recovered; a separate list or a sequence number is needed");
        System.out.println("TreeMap code order   -> " + report(tree.values()).equals(wantCode) + " with no extra work");
        System.out.println("LinkedHashMap added  -> " + report(linked.values()).equals(wantAdded) + " with no extra work");
    }
}
