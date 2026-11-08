// Student B: one scenario for each exception + the full test table.
public class PortalDemo {
    public static void main(String[] args) {
        ExamController c = new ExamController();
        Object[][] t = {
            {"S1 valid submission",        "S1", "CSA09", "ans.pdf", 120L, 10},
            {"S2 window closed",           "S1", "CSA09", "ans.pdf", 120L, 13},
            {"S3 not registered",          "S9", "CSA09", "ans.pdf", 120L, 10},
            {"S4 file too large",          "S2", "CSA09", "big.pdf", 800L, 10},
            {"S5 blank file name (unchecked)", "S1", "CSA09", " ",   10L, 10},
        };
        for (Object[] r : t)
            System.out.printf("%-32s -> %s%n", r[0],
                c.submit((String) r[1], (String) r[2], (String) r[3], (Long) r[4], (Integer) r[5]));
    }
}
