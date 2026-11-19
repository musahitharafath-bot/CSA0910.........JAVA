public final class FacilityConfig {
    private final int[] capacities;

    public FacilityConfig(int[] capacities) {
        this.capacities = capacities.clone();      // defensive copy in
    }

    public int[] getCapacities() {
        return capacities.clone();                 // defensive copy out
    }

    public int totalSlots() {
        int total = 0;
        for (int c : capacities) total += c;
        return total;
    }

    public static void main(String[] args) {
        int[] input = {20, 30, 25, 40, 15};
        FacilityConfig cfg = new FacilityConfig(input);
        input[0] = 999;                            // caller edits its own array
        cfg.getCapacities()[1] = 0;                // caller edits the copy
        System.out.println(cfg.totalSlots());
    }
}
