import java.util.Scanner;

/** Task 2: Data types, jagged arrays and safe fee conversion. */
public class Task02Demo {
    public static void main(String[] args) {
        int[] slotsPerFloor = {3, 2, 4}; // Floors may have different slot counts.
        String[][] slots = new String[slotsPerFloor.length][];
        for (int f = 0; f < slotsPerFloor.length; f++) {
            slots[f] = new String[slotsPerFloor[f]];
        }

        int floor = 1;
        int slotNumber = 0;
        long entryEpochMinutes = 1000L; // long for a larger time count
        int durationMinutes = 75;
        double hourlyRate = 30.0;

        // Every started hour is charged: ceiling(minutes / 60), using integer arithmetic.
        int billableHours = durationMinutes / 60 + (durationMinutes % 60 == 0 ? 0 : 1);
        double fee = billableHours * hourlyRate;

        System.out.println("Floors: " + slots.length);
        System.out.println("Floor " + floor + " has " + slots[floor].length + " slots");
        System.out.println("Example slot location: (" + floor + ", " + slotNumber + ")");
        System.out.println("Entry time in epoch minutes: " + entryEpochMinutes);
        System.out.println("Duration: " + durationMinutes + " minutes");
        System.out.println("Billable hours: " + billableHours);
        System.out.println("Fee: Rs " + fee);

        // Risk example: 75 / 60 using integer division alone gives 1, not 1.25.
        System.out.println("Incorrect integer division 75/60 = " + (75 / 60));
        System.out.println("Correct billing rounds every started hour upward.");
    }
}
