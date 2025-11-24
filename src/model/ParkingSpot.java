package model;

import java.io.Serializable;

public class ParkingSpot implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int spotId;
    private boolean isOccupied;
    private double hourlyRate; // السعر في الساعة

    public ParkingSpot(int spotId) {
        this.spotId = spotId;
        this.isOccupied = false;
        this.hourlyRate = 5.0; // السعر الافتراضي 5 جنيه
    }
    
    public ParkingSpot(int spotId, double hourlyRate) {
        this.spotId = spotId;
        this.isOccupied = false;
        this.hourlyRate = hourlyRate;
    }

    public int getSpotId() { return spotId; }
    public boolean isOccupied() { return isOccupied; }
    public void occupy() { isOccupied = true; }
    public void release() { isOccupied = false; }
    
    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
}
