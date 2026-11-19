import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

enum VehicleType { TWO_WHEELER, CAR, ELECTRIC_CAR }
enum UserCategory { STUDENT, STAFF, VISITOR }

public class DataTypesDemo {
    public static void main(String[] args) {
        LocalDateTime entry = LocalDateTime.of(2026, 10, 26, 9, 15);
        LocalDateTime exit  = LocalDateTime.of(2026, 10, 26, 11, 25);

        long minutes = Duration.between(entry, exit).toMinutes();   // 130
        long hours   = minutes == 0 ? 0 : (minutes + 59) / 60;      // 3
        BigDecimal fee = new BigDecimal("30.00")
                .multiply(BigDecimal.valueOf(hours));

        System.out.println(minutes + " min -> " + hours + " h -> Rs " + fee);
        System.out.println(0.1 + 0.2);
        System.out.println(new BigDecimal("0.10").add(new BigDecimal("0.20")));
        System.out.println(VehicleType.valueOf("CAR") + " "
                + UserCategory.VISITOR.ordinal());
    }
}
