public class ParkingTests {
    private static int passed = 0, failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) { passed++; System.out.println("PASS " + name); }
        else           { failed++; System.out.println("FAIL " + name); }
    }

    private static boolean rejects(Runnable action) {
        try { action.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
    }

    public static void main(String[] args) {
        ElectricCar ev = new ElectricCar("TN03C3");

        check("T1  two-wheeler, 130 min = 30", new TwoWheeler("TN01A1").calculateFee(130) == 30.0);
        check("T2  car, 130 min = 90", new Car("TN02B2").calculateFee(130) == 90.0);
        check("T3  EV, 130 min + 10 kWh = 210", ev.calculateFee(130) + ev.chargingFee(10) == 210.0);
        check("T4  zero minutes = 0", new Car("TN02B2").calculateFee(0) == 0.0);
        check("T5  exactly 60 min = 1 hour", new Car("TN02B2").calculateFee(60) == 30.0);
        check("T15 61 min = 2 hours", new Car("TN02B2").calculateFee(61) == 60.0);
        check("T10 negative duration rejected", rejects(() -> new Car("TN02B2").calculateFee(-1)));
        check("T18 negative kWh rejected", rejects(() -> ev.chargingFee(-5)));
        check("T19 blank registration rejected", rejects(() -> new Car("  ")));

        ParkingFacility f = new ParkingFacility(new int[]{1});
        check("T6  last free slot allocated", f.allocate("A1") != null);
        check("T7  full facility returns null", f.allocate("A2") == null);
        check("T8  occupied slot released", f.release(0, 0));
        check("T9  second release fails", !f.release(0, 0));
        check("T11 invalid floor rejected", !f.release(3, 0));

        System.out.println(passed + " passed, " + failed + " failed");
    }
}
