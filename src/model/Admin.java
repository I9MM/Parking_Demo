package model;

import repository.UserRepository;
import security.AuthorizationService;
import security.Permission;
import security.UnauthorizedException;
import service.ParkingService;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class Admin extends User {
    private transient UserRepository userRepository;
    private transient List<User> operators = new ArrayList<>();

    public Admin(int id, String name, String password) {
        super(id, name, password, Role.ADMIN);
    }
    
    // Initialize repository after deserialization
    private void initRepository() {
        if (userRepository == null) {
            userRepository = new UserRepository();
            loadOperators();
        }
    }

    private void loadOperators() {
        try {
            operators = userRepository.loadAll();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing users found");
            operators = new ArrayList<>();
        }
    }

    // Operators management
    public void addOperator(User operator) throws UnauthorizedException {
        initRepository();
        AuthorizationService.getInstance().checkPermission(Permission.MANAGE_USERS);
        
        // Check if user already exists
        for (User u : operators) {
            if (u.getId() == operator.getId() || u.getName().equals(operator.getName())) {
                System.out.println("User already exists!");
                return;
            }
        }
        
        operators.add(operator);
        try {
            userRepository.save(operator);
            System.out.println("User " + operator.getName() + " added successfully");
        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }
    
    public void updateOperator(User operator) throws UnauthorizedException {
        initRepository();
        AuthorizationService.getInstance().checkPermission(Permission.MANAGE_USERS);
        for (int i = 0; i < operators.size(); i++) {
            if (operators.get(i).getId() == operator.getId()) {
                operators.set(i, operator);
                try {
                    userRepository.saveAll(operators);
                    System.out.println("User updated successfully");
                } catch (IOException e) {
                    System.out.println("Error updating user: " + e.getMessage());
                }
                return;
            }
        }
    }
    
    public void deleteOperator(User operator) throws UnauthorizedException {
        initRepository();
        AuthorizationService.getInstance().checkPermission(Permission.MANAGE_USERS);
        operators.removeIf(op -> op.getId() == operator.getId());
        try {
            userRepository.saveAll(operators);
            System.out.println("User deleted successfully");
        } catch (IOException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }
    
    public List<User> getAllOperators() throws UnauthorizedException {
        initRepository();
        AuthorizationService.getInstance().checkPermission(Permission.MANAGE_USERS);
        return new ArrayList<>(operators);
    }

    // Ticket creation
    public Ticket createTicket(String ownerName, String nId, String plateNumber) {
        Car car = new Car(ownerName, nId, plateNumber);
        return new Ticket(car);
    }
    
    public void viewAllTickets(ParkingService parkingService) throws UnauthorizedException {
        List<Ticket> tickets = parkingService.getAllTickets();
        System.out.println("Total tickets: " + tickets.size());
        for (Ticket ticket : tickets) {
            System.out.println("Ticket ID: " + ticket.getEntryId() + 
                             ", Car: " + ticket.getCar().getPlateNumber());
        }
    }
}
