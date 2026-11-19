package parking.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import parking.model.Car;
import parking.model.ElectricCar;
import parking.model.TwoWheeler;
import parking.model.Vehicle;
import parking.service.ParkingFacility;
import parking.service.ParkingService;

public class Main {
    public static void main(String[] args) throws Exception {
        String file = args.length > 0 ? args[0] : "input/events.txt";
        ParkingService service = new ParkingService(new ParkingFacility(new int[]{2, 1}));
        for (String line : Files.readAllLines(Path.of(file))) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\\s+");
            if (p[0].equals("ENTER")) {
                Vehicle v = switch (p[2]) {
                    case "TWO_WHEELER" -> new TwoWheeler(p[1]);
                    case "CAR" -> new Car(p[1]);
                    case "ELECTRIC_CAR" -> new ElectricCar(p[1]);
                    default -> throw new IllegalArgumentException("Unknown type " + p[2]);
                };
                System.out.println(service.enter(v, LocalDateTime.parse(p[3])));
            } else if (p[0].equals("EXIT")) {
                System.out.println(service.exit(p[1], LocalDateTime.parse(p[2]), Integer.parseInt(p[3])));
            }
        }
    }
}
