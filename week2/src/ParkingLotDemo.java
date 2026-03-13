import java.util.*;
import java.time.*;

enum SpotStatus { EMPTY, OCCUPIED, DELETED }

class ParkingSpot {
    String licensePlate;
    SpotStatus status;
    LocalDateTime entryTime;

    public ParkingSpot() {
        this.status = SpotStatus.EMPTY;
    }
}

class ParkingLot {

    private ParkingSpot[] spots;
    private int capacity;
    private int totalProbes = 0;
    private int parkedVehicles = 0;
    private Map<Integer, Integer> occupancyPerHour = new HashMap<>(); // hour -> count

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        spots = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) spots[i] = new ParkingSpot();
    }

    // Simple hash function: license plate → spot
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // Park vehicle
    public int parkVehicle(String licensePlate) {
        int hash = hash(licensePlate);
        int probes = 0;

        for (int i = 0; i < capacity; i++) {
            int idx = (hash + i) % capacity;
            probes++;

            if (spots[idx].status == SpotStatus.EMPTY || spots[idx].status == SpotStatus.DELETED) {
                spots[idx].licensePlate = licensePlate;
                spots[idx].status = SpotStatus.OCCUPIED;
                spots[idx].entryTime = LocalDateTime.now();

                totalProbes += probes;
                parkedVehicles++;

                int hour = spots[idx].entryTime.getHour();
                occupancyPerHour.put(hour, occupancyPerHour.getOrDefault(hour, 0) + 1);

                System.out.println("Assigned spot #" + idx + " (" + (probes - 1) + " probes)");
                return idx;
            }
        }

        System.out.println("Parking full! Cannot assign spot.");
        return -1;
    }

    // Exit vehicle
    public void exitVehicle(String licensePlate) {
        for (int i = 0; i < capacity; i++) {
            if (spots[i].status == SpotStatus.OCCUPIED && spots[i].licensePlate.equals(licensePlate)) {
                LocalDateTime exitTime = LocalDateTime.now();
                Duration duration = Duration.between(spots[i].entryTime, exitTime);

                double hours = duration.toMinutes() / 60.0;
                double fee = hours * 5; // $5 per hour

                spots[i].status = SpotStatus.DELETED;
                spots[i].licensePlate = null;
                spots[i].entryTime = null;
                parkedVehicles--;

                System.out.printf("Spot #%d freed, Duration: %.2f h, Fee: $%.2f\n", i, hours, fee);
                return;
            }
        }
        System.out.println("Vehicle not found!");
    }

    // Parking statistics
    public void getStatistics() {
        double occupancy = (parkedVehicles * 100.0) / capacity;
        double avgProbes = parkedVehicles == 0 ? 0 : totalProbes * 1.0 / parkedVehicles;

        int peakHour = occupancyPerHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);

        System.out.printf("Occupancy: %.2f%%, Avg Probes: %.2f, Peak Hour: %s\n",
                occupancy, avgProbes,
                peakHour == -1 ? "N/A" : peakHour + ":00 - " + (peakHour + 1) + ":00");
    }
}

public class ParkingLotDemo {

    public static void main(String[] args) throws InterruptedException {

        ParkingLot lot = new ParkingLot(500);

        // Park some vehicles
        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        Thread.sleep(2000); // simulate 2 seconds

        // Exit a vehicle
        lot.exitVehicle("ABC-1234");

        // Park another vehicle
        lot.parkVehicle("LMN-4567");

        // Display statistics
        lot.getStatistics();
    }
}