package model;

import security.AuthorizationService;
import security.Permission;
import security.UnauthorizedException;
import service.ParkingService;

public class ExitOperator extends User {
    public ExitOperator(int id, String name, String password) {
        super(id, name, password, Role.EXIT_OPERATOR);
    }

    public double processPayment(Ticket ticket, double hourlyRate) {
        try {
            AuthorizationService.getInstance().checkPermission(Permission.CALCULATE_PAYMENT);
            return ticket.calculatePayment(hourlyRate);
        } catch (UnauthorizedException e) {
            System.out.println("Error: " + e.getMessage());
            return 0;
        }
    }
    
    // For backward compatibility
    public double processPayment(Ticket ticket) {
        return processPayment(ticket, 5.0);
    }
    
    public void processExit(ParkingService parkingService, int ticketId) {
        try {
            parkingService.processExit(ticketId);
        } catch (UnauthorizedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
