import java.util.Scanner;

public class InputDemo {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String regNo = in.nextLine().trim();
        int minutes = in.nextInt();
        if (minutes < 0) {
            System.out.println("Invalid duration");
            return;
        }
        int hours = minutes == 0 ? 0 : (minutes + 59) / 60;
        System.out.println(regNo + ": " + minutes + " min -> " + hours + " started hours -> Rs " + hours * 30.0);
    }
}
