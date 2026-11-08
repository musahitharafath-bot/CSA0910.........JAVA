// Student B: seven inputs, each intended to trigger a different path.
public class AtmTests {
    public static void main(String[] args) {
        String f = "input/cardreader.txt";
        String[][] t = {
            {"T1 valid withdrawal",  f, "ACC1001", "1500"},
            {"T2 letters as amount", f, "ACC1001", "abc"},
            {"T3 negative amount",   f, "ACC1001", "-300"},
            {"T4 more than balance", f, "ACC1002", "900"},
            {"T5 card file missing", "input/nofile.txt", "ACC1001", "100"},
            {"T6 unknown account",   f, "ACC9999", "100"},
            {"T7 empty amount",      f, "ACC1001", ""},
        };
        for (String[] c : t) {
            System.out.println(c[0] + "  (acc=" + c[2] + ", amt='" + c[3] + "')");
            System.out.println("   -> " + Atm.run(c[1], c[2], c[3]));
        }
    }
}
