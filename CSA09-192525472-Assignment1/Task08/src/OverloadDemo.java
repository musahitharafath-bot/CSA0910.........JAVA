import java.time.Duration;
import java.time.LocalDateTime;

class Billing {
    // Overload 1: the duration is already known in minutes
    static double calculateFee(int minutes, double ratePerHour) {
        if (minutes < 0) throw new IllegalArgumentException("Negative duration");
        int hours = minutes == 0 ? 0 : (minutes + 59) / 60;
        return hours * ratePerHour;
    }

    // Overload 2: the duration is derived from entry and exit timestamps
    static double calculateFee(LocalDateTime in, LocalDateTime out,
                               double ratePerHour) {
        long minutes = Duration.between(in, out).toMinutes();
        return calculateFee((int) minutes, ratePerHour);
    }
}

public class OverloadDemo {
    public static void main(String[] args) {
        LocalDateTime in  = LocalDateTime.of(2026, 10, 26, 9, 0);
        LocalDateTime out = LocalDateTime.of(2026, 10, 26, 11, 10);
        System.out.println(Billing.calculateFee(130, 30.0));
        System.out.println(Billing.calculateFee(in, out, 30.0));
    }
}
