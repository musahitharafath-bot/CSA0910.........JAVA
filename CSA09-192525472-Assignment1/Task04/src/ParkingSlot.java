public class ParkingSlot {
    private final int floor;
    private final int number;
    private String occupiedBy;            // null means the slot is free

    public ParkingSlot(int floor, int number) {
        if (floor < 0 || number < 0)
            throw new IllegalArgumentException("Negative index");
        this.floor = floor;
        this.number = number;
    }

    public int getFloor()  { return floor; }
    public int getNumber() { return number; }
    public boolean isFree() { return occupiedBy == null; }

    public boolean occupy(String regNo) {
        if (regNo == null || regNo.isBlank() || !isFree()) return false;
        occupiedBy = regNo;
        return true;
    }

    public boolean vacate() {
        if (isFree()) return false;
        occupiedBy = null;
        return true;
    }

    @Override
    public String toString() {
        return "Slot[F" + floor + "-S" + number
                + (isFree() ? ", free]" : ", " + occupiedBy + "]");
    }
}
