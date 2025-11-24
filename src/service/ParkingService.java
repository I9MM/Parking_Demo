package service;

import model.ParkingSpot;
import model.Ticket;
import repository.ParkingSpotRepository;
import repository.TicketRepository;
import security.AuthorizationService;
import security.Permission;
import security.UnauthorizedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParkingService {
    private ParkingSpotRepository spotRepository;
    private TicketRepository ticketRepository;
    private AuthorizationService authService;
    
    private List<ParkingSpot> spots = new ArrayList<>();
    private List<Ticket> tickets = new ArrayList<>();

    public ParkingService() {
        this.spotRepository = new ParkingSpotRepository();
        this.ticketRepository = new TicketRepository();
        this.authService = AuthorizationService.getInstance();
        loadData();
    }

    private void loadData() {
        try {
            spots = spotRepository.loadAll();
            tickets = ticketRepository.loadAll();
            System.out.println("Data loaded: " + spots.size() + " spots, " + tickets.size() + " tickets");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing data found, starting fresh");
        }
    }

    public void addSpot(ParkingSpot spot) throws UnauthorizedException {
        authService.checkPermission(Permission.ADD_PARKING_SPOT);
        
        // Check if spot already exists
        for (ParkingSpot s : spots) {
            if (s.getSpotId() == spot.getSpotId()) {
                System.out.println("Spot " + spot.getSpotId() + " already exists!");
                return;
            }
        }
        
        spots.add(spot);
        try {
            spotRepository.save(spot);
            System.out.println("Parking spot " + spot.getSpotId() + " added successfully");
        } catch (IOException e) {
            System.out.println("Error saving parking spot: " + e.getMessage());
        }
    }
    
    public void removeSpot(int spotId) throws UnauthorizedException {
        authService.checkPermission(Permission.REMOVE_PARKING_SPOT);
        
        ParkingSpot toRemove = null;
        for (ParkingSpot s : spots) {
            if (s.getSpotId() == spotId) {
                toRemove = s;
                break;
            }
        }
        
        if (toRemove != null) {
            spots.remove(toRemove);
            try {
                spotRepository.saveAll(spots);
                System.out.println("Parking spot " + spotId + " removed successfully");
            } catch (IOException e) {
                System.out.println("Error removing parking spot: " + e.getMessage());
            }
        } else {
            System.out.println("Spot not found!");
        }
    }

    public void showFreeSpots() throws UnauthorizedException {
        authService.checkPermission(Permission.VIEW_FREE_SPOTS);
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied())
                System.out.println("Free spot: " + spot.getSpotId());
        }
    }

    public ParkingSpot getNextFreeSpot() throws UnauthorizedException {
        authService.checkPermission(Permission.ISSUE_TICKET);
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied()) {
                spot.occupy();
                try {
                    spotRepository.saveAll(spots);
                } catch (IOException e) {
                    System.out.println("Error updating parking spot: " + e.getMessage());
                }
                return spot;
            }
        }
        return null;
    }

    public void assignTicket(Ticket ticket) throws UnauthorizedException {
        authService.checkPermission(Permission.ISSUE_TICKET);
        tickets.add(ticket);
        try {
            ticketRepository.save(ticket);
            System.out.println("Ticket " + ticket.getEntryId() + " issued successfully");
        } catch (IOException e) {
            System.out.println("Error saving ticket: " + e.getMessage());
        }
    }

    public void processExit(int ticketId) throws UnauthorizedException {
        authService.checkPermission(Permission.PROCESS_EXIT);
        Ticket ticket = ticketRepository.findById(ticketId);
        if (ticket != null) {
            double payment = ticket.calculatePayment();
            System.out.println("Payment calculated: " + payment);
        } else {
            System.out.println("Ticket not found");
        }
    }

    public List<Ticket> getAllTickets() throws UnauthorizedException {
        authService.checkPermission(Permission.VIEW_ALL_TICKETS);
        return tickets;
    }
    
    public List<Ticket> getAllTicketsForExit() {
        // Exit operators can view tickets without full permission
        return new ArrayList<>(tickets);
    }

    public List<ParkingSpot> getAllSpots() {
        return spots;
    }
    
    public Ticket findTicketById(int ticketId) {
        return ticketRepository.findById(ticketId);
    }
    
    public void deleteTicket(int ticketId) throws UnauthorizedException {
        authService.checkPermission(Permission.VIEW_ALL_TICKETS); // Admin only
        
        Ticket toRemove = null;
        for (Ticket t : tickets) {
            if (t.getEntryId() == ticketId) {
                toRemove = t;
                break;
            }
        }
        
        if (toRemove != null) {
            tickets.remove(toRemove);
            try {
                ticketRepository.saveAll(tickets);
                System.out.println("Ticket " + ticketId + " deleted successfully");
            } catch (IOException e) {
                System.out.println("Error deleting ticket: " + e.getMessage());
            }
        }
    }
    
    public void confirmPayment(int ticketId) throws UnauthorizedException {
        authService.checkPermission(Permission.PROCESS_EXIT);
        
        Ticket ticket = findTicketById(ticketId);
        if (ticket != null) {
            // Release the parking spot
            int spotId = ticket.getSpotId();
            for (ParkingSpot spot : spots) {
                if (spot.getSpotId() == spotId) {
                    spot.release();
                    try {
                        spotRepository.saveAll(spots);
                        System.out.println("Spot " + spotId + " released");
                    } catch (IOException e) {
                        System.out.println("Error releasing spot: " + e.getMessage());
                    }
                    break;
                }
            }
            
            // Remove the ticket from active tickets
            tickets.remove(ticket);
            try {
                ticketRepository.saveAll(tickets);
                System.out.println("Payment confirmed for ticket " + ticketId);
            } catch (IOException e) {
                System.out.println("Error removing ticket: " + e.getMessage());
            }
        }
    }
    
    public void saveSpotsData() throws IOException {
        spotRepository.saveAll(spots);
    }
}
