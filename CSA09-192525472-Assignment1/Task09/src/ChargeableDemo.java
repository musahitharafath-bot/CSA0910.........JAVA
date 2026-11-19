import java.util.List;

interface Chargeable {
    double chargingFee(int kWh);

    default boolean supportsFastCharging() { return false; }

    static double standardRatePerKWh() { return 12.0; }
}

public class ChargeableDemo {
    public static void main(String[] args) {
        Chargeable standard   = kWh -> kWh * Chargeable.standardRatePerKWh();
        Chargeable discounted = kWh -> kWh * 9.0;

        List<Chargeable> tariffs = List.of(standard, discounted);
        for (Chargeable t : tariffs) {
            System.out.println(t.chargingFee(10) + " " + t.supportsFastCharging());
        }
    }
}
