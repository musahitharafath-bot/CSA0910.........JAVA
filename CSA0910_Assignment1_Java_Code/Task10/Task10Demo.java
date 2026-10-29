/*
 * Task 10: Access modifiers and validation.
 * In a real multi-file project, split these classes into packages such as
 * parking.model, parking.service, and parking.app.
 */
public class Task10Demo {
    static class ParkingSlot {
        private final int floor;
        private final int slotNumber;
        private String registrationNumber;

        public ParkingSlot(int floor, int slotNumber) {
            if (floor < 0 || slotNumber < 0)
                throw new IllegalArgumentException("Floor and slot must be non-negative");
            this.floor = floor;
            this.slotNumber = slotNumber;
        }

        public int getFloor() { return floor; }
        public int getSlotNumber() { return slotNumber; }
        public String getRegistrationNumber() { return registrationNumber; }

        public void park(String regNo) {
            if (regNo == null || regNo.isBlank())
                throw new IllegalArgumentException("Registration number required");
            if (registrationNumber != null)
                throw new IllegalStateException("Slot is already occupied");
            registrationNumber = regNo;
        }

        public void release() {
            if (registrationNumber == null) throw new IllegalStateException("Slot is already free");
            registrationNumber = null;
        }
    }

    public static void main(String[] args) {
        ParkingSlot slot = new ParkingSlot(0, 3);
        slot.park("TN01AB1111");
        System.out.println("Floor: " + slot.getFloor());
        System.out.println("Slot: " + slot.getSlotNumber());
        System.out.println("Parked vehicle: " + slot.getRegistrationNumber());
        slot.release();
        System.out.println("Free after release: " + (slot.getRegistrationNumber() == null));
        try {
            slot.park("");
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid registration rejected: " + ex.getMessage());
        }
    }
}
