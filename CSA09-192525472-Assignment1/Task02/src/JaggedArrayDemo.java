public class JaggedArrayDemo {
    public static void main(String[] args) {
        String[][] slots = new String[3][];
        slots[0] = new String[20];
        slots[1] = new String[30];
        slots[2] = new String[50];
        slots[1][4] = "TN01A1";               // occupied; null means free
        int total = 0;
        for (int f = 0; f < slots.length; f++) {
            int free = 0;
            for (String s : slots[f]) if (s == null) free++;
            System.out.println("Floor " + f + ": " + slots[f].length + " slots, " + free + " free");
            total += slots[f].length;
        }
        System.out.println("Total slots: " + total);
    }
}
