public class Task01Demo {
    static class Vehicle {
        String regNo; String type; long entryTime;
        Vehicle(String regNo, String type, long entryTime) {
            this.regNo = regNo; this.type = type; this.entryTime = entryTime;
        }
    }

    public static void main(String[] args) {
        // Object representation: related data stays together
        Vehicle[] objects = {
            new Vehicle("TN01A1", "TWO_WHEELER", 900),
            new Vehicle("TN02B2", "CAR", 905),
            new Vehicle("TN03C3", "ELECTRIC_CAR", 910)
        };
        // Parallel arrays: every attribute is a separate array that must stay aligned
        String[] regNo = {"TN01A1", "TN02B2", "TN03C3"};
        String[] type = {"TWO_WHEELER", "CAR", "ELECTRIC_CAR"};
        long[] entryTime = {900, 905, 910};

        String search = "TN03C3";
        for (Vehicle v : objects)
            if (v.regNo.equals(search)) System.out.println("Object search   -> " + v.type + " @" + v.entryTime);
        for (int i = 0; i < regNo.length; i++)
            if (regNo[i].equals(search)) System.out.println("Parallel search -> " + type[i] + " @" + entryTime[i]);
    }
}
