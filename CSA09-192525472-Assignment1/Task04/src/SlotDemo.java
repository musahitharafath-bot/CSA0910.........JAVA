public class SlotDemo {
    public static void main(String[] args) {
        ParkingSlot s = new ParkingSlot(1, 4);
        System.out.println(s);
        System.out.println(s.occupy("TN01A1"));
        System.out.println(s.occupy("TN02B2"));
        System.out.println(s);
        System.out.println(s.vacate() + " " + s.vacate());
    }
}
