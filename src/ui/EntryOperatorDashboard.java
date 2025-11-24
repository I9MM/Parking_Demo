package ui;

import model.Car;
import model.EntryOperator;
import model.ParkingSpot;
import model.Ticket;
import security.AuthorizationService;
import security.UnauthorizedException;
import service.ParkingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EntryOperatorDashboard extends JFrame {
    private ParkingService parkingService;
    private EntryOperator operator;
    private JTable spotsTable;
    private DefaultTableModel spotsModel;
    private JTextField ownerField;
    private JTextField nidField;
    private JTextField plateField;

    public EntryOperatorDashboard(ParkingService parkingService, EntryOperator operator) {
        this.parkingService = parkingService;
        this.operator = operator;
        initializeUI();
        loadFreeSpots();
    }

    private void initializeUI() {
        setTitle("Entry Operator Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(46, 204, 113));
        JLabel headerLabel = new JLabel("Entry Operator Dashboard");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 22));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Free Spots Panel
        JPanel spotsPanel = new JPanel(new BorderLayout());
        spotsPanel.setBorder(BorderFactory.createTitledBorder("Available Parking Spots"));
        
        String[] columns = {"Spot ID", "Status", "Rate (EGP/hr)"};
        spotsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        spotsTable = new JTable(spotsModel);
        spotsTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(spotsTable);
        spotsPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadFreeSpots());
        JPanel refreshPanel = new JPanel();
        refreshPanel.add(refreshButton);
        spotsPanel.add(refreshPanel, BorderLayout.SOUTH);

        mainPanel.add(spotsPanel, BorderLayout.CENTER);

        // Issue Ticket Panel
        JPanel ticketPanel = createTicketPanel();
        mainPanel.add(ticketPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel();
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> logout());
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTicketPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Issue New Ticket"));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Owner Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Owner Name:"), gbc);
        ownerField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(ownerField, gbc);

        // National ID
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("National ID:"), gbc);
        nidField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(nidField, gbc);

        // Plate Number
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Plate Number:"), gbc);
        plateField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(plateField, gbc);
        
        // Spot Selection
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Parking Spot:"), gbc);
        
        JPanel spotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JRadioButton autoButton = new JRadioButton("Auto", true);
        JRadioButton manualButton = new JRadioButton("Manual");
        ButtonGroup group = new ButtonGroup();
        group.add(autoButton);
        group.add(manualButton);
        
        JComboBox<String> spotCombo = new JComboBox<>();
        spotCombo.setEnabled(false);
        updateSpotCombo(spotCombo);
        
        autoButton.addActionListener(e -> spotCombo.setEnabled(false));
        manualButton.addActionListener(e -> {
            spotCombo.setEnabled(true);
            updateSpotCombo(spotCombo);
        });
        
        spotPanel.add(autoButton);
        spotPanel.add(manualButton);
        spotPanel.add(spotCombo);
        
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(spotPanel, gbc);

        // Issue Button
        JButton issueButton = new JButton("Issue Ticket");
        issueButton.setBackground(new Color(52, 152, 219));
        issueButton.setForeground(Color.WHITE);
        issueButton.addActionListener(e -> issueTicket(autoButton.isSelected(), spotCombo));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(issueButton, gbc);

        return panel;
    }
    
    private void updateSpotCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        List<ParkingSpot> spots = parkingService.getAllSpots();
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied()) {
                combo.addItem("Spot " + spot.getSpotId() + " (" + String.format("%.2f", spot.getHourlyRate()) + " EGP/hr)");
            }
        }
    }

    private void loadFreeSpots() {
        spotsModel.setRowCount(0);
        List<ParkingSpot> spots = parkingService.getAllSpots();
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied()) {
                Object[] row = {spot.getSpotId(), "Free", String.format("%.2f", spot.getHourlyRate())};
                spotsModel.addRow(row);
            }
        }
    }

    private void issueTicket(boolean isAuto, JComboBox<String> spotCombo) {
        String owner = ownerField.getText().trim();
        String nid = nidField.getText().trim();
        String plate = plateField.getText().trim();

        if (owner.isEmpty() || nid.isEmpty() || plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill all fields!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Car car = new Car(owner, nid, plate);
            ParkingSpot spot;
            
            if (isAuto) {
                // Auto select spot
                spot = parkingService.getNextFreeSpot();
            } else {
                // Manual select spot
                String selected = (String) spotCombo.getSelectedItem();
                if (selected == null) {
                    JOptionPane.showMessageDialog(this, 
                        "No spots available!", 
                        "Error", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Extract spot ID from "Spot 101 (5.00 EGP/hr)" format
                String spotIdStr = selected.substring(selected.indexOf("Spot ") + 5, selected.indexOf(" ("));
                int spotId = Integer.parseInt(spotIdStr.trim());
                spot = null;
                for (ParkingSpot s : parkingService.getAllSpots()) {
                    if (s.getSpotId() == spotId && !s.isOccupied()) {
                        s.occupy();
                        spot = s;
                        break;
                    }
                }
            }
            
            if (spot != null) {
                Ticket ticket = new Ticket(car);
                ticket.setSpotId(spot.getSpotId()); // Save spot ID to ticket
                parkingService.assignTicket(ticket);
                
                JOptionPane.showMessageDialog(this, 
                    "Ticket Issued Successfully!\n\n" +
                    "Ticket ID: " + ticket.getEntryId() + "\n" +
                    "Assigned Spot: " + spot.getSpotId() + "\n" +
                    "Plate: " + plate + "\n" +
                    "Owner: " + owner, 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Clear fields
                ownerField.setText("");
                nidField.setText("");
                plateField.setText("");
                
                loadFreeSpots();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No free parking spots available!", 
                    "Error", 
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (UnauthorizedException e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        AuthorizationService.getInstance().logout();
        new LoginFrame(parkingService).setVisible(true);
        dispose();
    }
}
