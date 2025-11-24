package repository;

import model.ParkingSpot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingSpotRepository implements Repository<ParkingSpot> {
    private static final String FILE_PATH = "data/parking_spots.txt";
    private List<ParkingSpot> spots;

    public ParkingSpotRepository() {
        this.spots = new ArrayList<>();
        createDataDirectory();
    }

    private void createDataDirectory() {
        File dir = new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void save(ParkingSpot spot) throws IOException {
        // Check if spot already exists
        boolean exists = false;
        for (ParkingSpot s : spots) {
            if (s.getSpotId() == spot.getSpotId()) {
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            spots.add(spot);
        }
        saveAll(spots);
    }

    @Override
    public void saveAll(List<ParkingSpot> entities) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (ParkingSpot spot : entities) {
                // Format: spotId,isOccupied
                writer.write(spot.getSpotId() + "," + spot.isOccupied());
                writer.newLine();
            }
        }
    }

    @Override
    public List<ParkingSpot> loadAll() throws IOException, ClassNotFoundException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        spots = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    int spotId = Integer.parseInt(parts[0]);
                    boolean isOccupied = Boolean.parseBoolean(parts[1]);
                    
                    ParkingSpot spot = new ParkingSpot(spotId);
                    if (isOccupied) {
                        spot.occupy();
                    }
                    spots.add(spot);
                }
            }
        }
        return spots;
    }

    @Override
    public void delete(ParkingSpot spot) throws IOException {
        spots.remove(spot);
        saveAll(spots);
    }

    @Override
    public void clear() throws IOException {
        spots.clear();
        saveAll(spots);
    }

    public ParkingSpot findById(int spotId) {
        for (ParkingSpot spot : spots) {
            if (spot.getSpotId() == spotId) {
                return spot;
            }
        }
        return null;
    }

    public List<ParkingSpot> findFreeSpots() {
        List<ParkingSpot> freeSpots = new ArrayList<>();
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied()) {
                freeSpots.add(spot);
            }
        }
        return freeSpots;
    }
}
