public class DailyCollection {
    public static void main(String[] args) {
        double parking = 0, charging = 0;

        Vehicle twoWheeler = new TwoWheeler("TN01A1");   // 130 min
        Vehicle car        = new Car("TN02B2");          // 61 min
        ElectricCar ev     = new ElectricCar("TN03C3");  // 130 min, 10 kWh
        Vehicle visitor    = new Car("TN04D4");          // 30 min

        parking += twoWheeler.calculateFee(130);
        parking += car.calculateFee(61);
        parking += ev.calculateFee(130);
        charging += ev.chargingFee(10);
        parking += visitor.calculateFee(30);

        System.out.println("Parking  : Rs " + parking);
        System.out.println("Charging : Rs " + charging);
        System.out.println("Total    : Rs " + (parking + charging));
    }
}
