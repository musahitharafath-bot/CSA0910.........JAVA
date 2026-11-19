import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

interface Pass {
    boolean isValidAt(LocalDateTime time);
    boolean waivesParkingFee();
}

class OneDayPass implements Pass {
    private final LocalDateTime validFrom;
    private final LocalDateTime validUntil;

    OneDayPass(LocalDateTime validFrom) {
        this.validFrom = validFrom;
        this.validUntil = validFrom.plusDays(1);
    }

    @Override
    public boolean isValidAt(LocalDateTime time) {
        return !time.isBefore(validFrom) && time.isBefore(validUntil);
    }

    @Override
    public boolean waivesParkingFee() {
        return true;
    }
}

class MonthlyPass implements Pass {
    private final LocalDateTime validFrom;
    private final LocalDateTime validUntil;

    MonthlyPass(LocalDateTime validFrom) {
        this.validFrom = validFrom;
        this.validUntil = validFrom.plusMonths(1);
    }

    @Override
    public boolean isValidAt(LocalDateTime time) {
        return !time.isBefore(validFrom) && time.isBefore(validUntil);
    }

    @Override
    public boolean waivesParkingFee() {
        return true;
    }
}

interface PricingRule {
    BigDecimal calculate(long minutes, LocalDateTime entry,
                         LocalDateTime exit);
}

class FlatFeeRule implements PricingRule {
    private final BigDecimal flatFee;

    FlatFeeRule(BigDecimal flatFee) {
        if (flatFee.signum() < 0) throw new IllegalArgumentException();
        this.flatFee = flatFee;
    }

    @Override
    public BigDecimal calculate(long minutes, LocalDateTime entry,
                                LocalDateTime exit) {
        return minutes == 0 ? BigDecimal.ZERO : flatFee;
    }
}

class DeliveryVanRule implements PricingRule {
    private static final LocalTime PEAK_START = LocalTime.of(9, 0);
    private static final LocalTime PEAK_END   = LocalTime.of(17, 0);
    private final BigDecimal carHourlyRate;

    DeliveryVanRule(BigDecimal carHourlyRate) {
        if (carHourlyRate == null || carHourlyRate.signum() < 0)
            throw new IllegalArgumentException("Invalid rate");
        this.carHourlyRate = carHourlyRate;
    }

    @Override
    public BigDecimal calculate(long minutes, LocalDateTime entry,
                                LocalDateTime exit) {
        if (minutes < 0 || exit.isBefore(entry))
            throw new IllegalArgumentException("Invalid interval");
        if (minutes == 0) return BigDecimal.ZERO.setScale(2);

        // Minutes that overlap the 09:00-17:00 window on each calendar day.
        long peakMinutes = 0;
        for (LocalDate d = entry.toLocalDate();
             !d.isAfter(exit.toLocalDate()); d = d.plusDays(1)) {
            LocalDateTime windowStart = d.atTime(PEAK_START);
            LocalDateTime windowEnd   = d.atTime(PEAK_END);
            LocalDateTime from = entry.isAfter(windowStart) ? entry : windowStart;
            LocalDateTime to   = exit.isBefore(windowEnd) ? exit : windowEnd;
            if (from.isBefore(to)) {
                peakMinutes += Duration.between(from, to).toMinutes();
            }
        }
        long offPeakMinutes = minutes - peakMinutes;

        BigDecimal peakFee = carHourlyRate.multiply(BigDecimal.valueOf(2))
                .multiply(BigDecimal.valueOf(startedHours(peakMinutes)));
        BigDecimal offPeakFee = carHourlyRate
                .multiply(BigDecimal.valueOf(startedHours(offPeakMinutes)));
        return peakFee.add(offPeakFee).setScale(2, RoundingMode.HALF_UP);
    }

    private static long startedHours(long minutes) {
        return minutes <= 0 ? 0 : (minutes + 59) / 60;
    }
}

class EBike {
    private final PricingRule pricingRule;

    EBike(PricingRule pricingRule) {
        this.pricingRule = pricingRule;
    }

    BigDecimal calculateFee(long minutes, LocalDateTime entry,
                            LocalDateTime exit) {
        return pricingRule.calculate(minutes, entry, exit);
    }
}
