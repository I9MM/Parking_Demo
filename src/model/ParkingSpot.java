package model;

import java.io.Serializable;

public class ParkingSpot implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int spotId;
    private boolean isOccupied;

    public ParkingSpot(int spotId) {
        this.spotId = spotId;
        this.isOccupied = false;
    }

    public int getSpotId() { return spotId; }
    public boolean isOccupied() { return isOccupied; }
    public void occupy() { isOccupied = true; }
    public void release() { isOccupied = false; }
}
