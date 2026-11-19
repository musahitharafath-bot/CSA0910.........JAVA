import java.math.BigDecimal;
import java.time.LocalDateTime;
public class VanCases {
    public static void main(String[] a) {
        DeliveryVanRule v = new DeliveryVanRule(new BigDecimal("30"));
        int[][] c = {{8,30,10,0},{10,0,12,10},{17,0,18,0},{16,30,17,30},{9,0,17,0},{7,0,9,0}};
        for (int[] x : c) {
            LocalDateTime i = LocalDateTime.of(2026,10,26,x[0],x[1]);
            LocalDateTime o = LocalDateTime.of(2026,10,26,x[2],x[3]);
            System.out.println(x[0]+":"+x[1]+"-"+x[2]+":"+x[3]+" -> "+v.calculate(Task12Demo.mins(i,o), i, o));
        }
        LocalDateTime i = LocalDateTime.of(2026,10,26,16,0), o = LocalDateTime.of(2026,10,27,10,0);
        System.out.println("overnight 16:00-10:00 -> " + v.calculate(Task12Demo.mins(i,o), i, o));
        MonthlyPass m = new MonthlyPass(LocalDateTime.of(2026,10,1,0,0));
        System.out.println(m.isValidAt(LocalDateTime.of(2026,10,31,23,59)) + " " + m.isValidAt(LocalDateTime.of(2026,11,1,0,0)));
    }
}
