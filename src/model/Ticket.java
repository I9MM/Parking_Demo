package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static int counter = 1;
    private int entryId;
    private Car car;
    private LocalDateTime timeIn;
    private LocalDateTime timeOut;
    private double payment;
    private int spotId;

    public Ticket(Car car) {
        this.entryId = counter++;
        this.car = car;
        this.timeIn = LocalDateTime.now();
    }

    public double calculatePayment(double hourlyRate) {
        if (timeOut == null) timeOut = LocalDateTime.now();
        Duration duration = Duration.between(timeIn, timeOut);
        long hours = duration.toHours() + 1; // round up
        payment = hours * hourlyRate;
        return payment;
    }
    
    // For backward compatibility
    public double calculatePayment() {
        return calculatePayment(5.0); // Default rate
    }

    public int getEntryId() { return entryId; }
    public Car getCar() { return car; }
    public LocalDateTime getTimeIn() { return timeIn; }
    public LocalDateTime getTimeOut() { return timeOut; }
    public void setTimeOut(LocalDateTime timeOut) { this.timeOut = timeOut; }
    public double getPayment() { return payment; }
    public void setPayment(double payment) { this.payment = payment; }
    
    public int getSpotId() { return spotId; }
    public void setSpotId(int spotId) { this.spotId = spotId; }
}
