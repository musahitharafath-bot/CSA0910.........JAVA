/** Task 9: abstract class and multiple interface behaviours. */
public class Task09Demo {
    interface Chargeable {
        double chargingFee(int kWh);
        default String chargingMessage() { return "EV charging service available"; }
        static boolean validEnergy(int kWh) { return kWh >= 0; }
    }
    interface Payable {
        double pay();
    }
    static abstract class Vehicle {
        private final String regNo;
        Vehicle(String regNo) { this.regNo = regNo; }
        public String getRegNo() { return regNo; }
        public abstract double parkingFee(int minutes);
    }
    static class ElectricCar extends Vehicle implements Chargeable, Payable {
        ElectricCar(String regNo) { super(regNo); }
        @Override public double parkingFee(int minutes) {
            if (minutes < 0) throw new IllegalArgumentException("Negative duration");
            int hours = minutes / 60 + (minutes % 60 == 0 ? 0 : 1);
            return hours * 30.0;
        }
        @Override public double chargingFee(int kWh) {
            if (!Chargeable.validEnergy(kWh)) throw new IllegalArgumentException("Negative kWh");
            return kWh * 12.0;
        }
        @Override public double pay() { return parkingFee(90) + chargingFee(5); }
    }

    public static void main(String[] args) {
        ElectricCar ev = new ElectricCar("TN03C3");
        System.out.println(ev.getRegNo());
        System.out.println(ev.chargingMessage());
        System.out.println("Parking fee: Rs " + ev.parkingFee(90));
        System.out.println("Charging fee: Rs " + ev.chargingFee(5));
        System.out.println("Total payable: Rs " + ev.pay());
    }
}
