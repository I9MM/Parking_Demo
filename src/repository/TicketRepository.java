package repository;

import model.Car;
import model.Ticket;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketRepository implements Repository<Ticket> {
    private static final String FILE_PATH = "data/tickets.txt";
    private List<Ticket> tickets;

    public TicketRepository() {
        this.tickets = new ArrayList<>();
        createDataDirectory();
    }

    private void createDataDirectory() {
        File dir = new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void save(Ticket ticket) throws IOException {
        // Check if ticket already exists
        boolean exists = false;
        for (Ticket t : tickets) {
            if (t.getEntryId() == ticket.getEntryId()) {
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            tickets.add(ticket);
        }
        saveAll(tickets);
    }

    @Override
    public void saveAll(List<Ticket> entities) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Ticket ticket : entities) {
                // Format: entryId|ownerName|nId|plateNumber|timeIn|payment
                writer.write(ticket.getEntryId() + "|" +
                           ticket.getCar().getOwnerName() + "|" +
                           ticket.getCar().getNId() + "|" +
                           ticket.getCar().getPlateNumber() + "|" +
                           ticket.getTimeIn().toString() + "|" +
                           ticket.getPayment());
                writer.newLine();
            }
        }
    }

    @Override
    public List<Ticket> loadAll() throws IOException, ClassNotFoundException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        tickets = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    String ownerName = parts[1];
                    String nId = parts[2];
                    String plateNumber = parts[3];
                    
                    Car car = new Car(ownerName, nId, plateNumber);
                    Ticket ticket = new Ticket(car);
                    tickets.add(ticket);
                }
            }
        }
        return tickets;
    }

    @Override
    public void delete(Ticket ticket) throws IOException {
        tickets.remove(ticket);
        saveAll(tickets);
    }

    @Override
    public void clear() throws IOException {
        tickets.clear();
        saveAll(tickets);
    }

    public Ticket findById(int entryId) {
        for (Ticket ticket : tickets) {
            if (ticket.getEntryId() == entryId) {
                return ticket;
            }
        }
        return null;
    }

    public List<Ticket> findByPlateNumber(String plateNumber) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.getCar().getPlateNumber().equals(plateNumber)) {
                result.add(ticket);
            }
        }
        return result;
    }
}
