import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class Task12Demo {
    static long mins(LocalDateTime a, LocalDateTime b) {
        return Duration.between(a, b).toMinutes();
    }

    public static void main(String[] args) {
        LocalDateTime in  = LocalDateTime.of(2026, 10, 26, 8, 30);
        LocalDateTime out = LocalDateTime.of(2026, 10, 26, 10, 0);

        EBike bike = new EBike(new FlatFeeRule(new BigDecimal("15")));
        DeliveryVanRule van = new DeliveryVanRule(new BigDecimal("30"));
        OneDayPass pass = new OneDayPass(LocalDateTime.of(2026, 10, 26, 0, 0));

        System.out.println("E-bike 90 min : " + bike.calculateFee(mins(in, out), in, out));
        System.out.println("Van 08:30-10:00: " + van.calculate(mins(in, out), in, out));
        System.out.println("Pass valid now : " + pass.isValidAt(in));
        System.out.println("Pass next day  : " + pass.isValidAt(in.plusDays(1)));
    }
}
