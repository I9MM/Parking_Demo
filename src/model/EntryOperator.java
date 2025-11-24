package model;

import security.UnauthorizedException;
import service.ParkingService;

public class EntryOperator extends User {
    public EntryOperator(int id, String name, String password) {
        super(id, name, password, Role.ENTRY_OPERATOR);
    }

    public void monitorFreeSpots(ParkingService parkingService) {
        try {
            parkingService.showFreeSpots();
        } catch (UnauthorizedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void adviseSpot(ParkingService parkingService) {
        try {
            ParkingSpot spot = parkingService.getNextFreeSpot();
            if (spot != null) {
                System.out.println("Advise customer to park at spot: " + spot.getSpotId());
            } else {
                System.out.println("No free spots available");
            }
        } catch (UnauthorizedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public void issueTicket(ParkingService parkingService, Car car) {
        try {
            Ticket ticket = new Ticket(car);
            parkingService.assignTicket(ticket);
            System.out.println("Ticket issued: " + ticket.getEntryId());
        } catch (UnauthorizedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
