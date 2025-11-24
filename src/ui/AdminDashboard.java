package ui;

import model.*;
import security.AuthorizationService;
import security.UnauthorizedException;
import service.ParkingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JFrame {
    private ParkingService parkingService;
    private Admin admin;
    private JTable ticketsTable;
    private JTable spotsTable;
    private JTable usersTable;
    private DefaultTableModel ticketsModel;
    private DefaultTableModel spotsModel;
    private DefaultTableModel usersModel;

    public AdminDashboard(ParkingService parkingService) {
        this.parkingService = parkingService;
        this.admin = new Admin(1, "admin", "admin123");
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setTitle("Admin Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        JLabel headerLabel = new JLabel("Admin Dashboard");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 22));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Parking Spots Tab
        tabbedPane.addTab("Parking Spots", createSpotsPanel());
        
        // Tickets Tab
        tabbedPane.addTab("All Tickets", createTicketsPanel());
        
        // Users Management Tab
        tabbedPane.addTab("Manage Users", createUsersPanel());
        
        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel();
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> logout());
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSpotsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table
        String[] columns = {"Spot ID", "Status"};
        spotsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        spotsTable = new JTable(spotsModel);
        spotsTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(spotsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("+ Add Spot");
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addParkingSpot());
        
        JButton removeButton = new JButton("- Remove Spot");
        removeButton.setBackground(new Color(231, 76, 60));
        removeButton.setForeground(Color.WHITE);
        removeButton.addActionListener(e -> removeParkingSpot());
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadSpotsData());
        
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTicketsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table
        String[] columns = {"Ticket ID", "Plate Number", "Owner", "Time In", "Status"};
        ticketsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ticketsTable = new JTable(ticketsModel);
        ticketsTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(ticketsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadTicketsData());
        
        JButton deleteButton = new JButton("Delete Ticket");
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deleteTicket());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table
        String[] columns = {"ID", "Username", "Role"};
        usersModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(usersModel);
        usersTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(usersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        
        JButton addUserButton = new JButton("+ Add User");
        addUserButton.setBackground(new Color(46, 204, 113));
        addUserButton.setForeground(Color.WHITE);
        addUserButton.addActionListener(e -> addUser());
        
        JButton deleteUserButton = new JButton("- Delete User");
        deleteUserButton.setBackground(new Color(231, 76, 60));
        deleteUserButton.setForeground(Color.WHITE);
        deleteUserButton.addActionListener(e -> deleteUser());
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadUsersData());
        
        buttonPanel.add(addUserButton);
        buttonPanel.add(deleteUserButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadData() {
        loadSpotsData();
        loadTicketsData();
        loadUsersData();
    }

    private void loadSpotsData() {
        spotsModel.setRowCount(0);
        List<ParkingSpot> spots = parkingService.getAllSpots();
        for (ParkingSpot spot : spots) {
            Object[] row = {
                spot.getSpotId(),
                spot.isOccupied() ? "Occupied" : "Free"
            };
            spotsModel.addRow(row);
        }
    }

    private void loadTicketsData() {
        ticketsModel.setRowCount(0);
        try {
            List<Ticket> tickets = parkingService.getAllTickets();
            for (Ticket ticket : tickets) {
                Object[] row = {
                    ticket.getEntryId(),
                    ticket.getCar().getPlateNumber(),
                    ticket.getCar().getOwnerName(),
                    ticket.getTimeIn().toString().substring(0, 16),
                    "Active"
                };
                ticketsModel.addRow(row);
            }
        } catch (UnauthorizedException e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addParkingSpot() {
        String spotIdStr = JOptionPane.showInputDialog(this, "Enter Spot ID:");
        if (spotIdStr != null && !spotIdStr.trim().isEmpty()) {
            try {
                int spotId = Integer.parseInt(spotIdStr);
                parkingService.addSpot(new ParkingSpot(spotId));
                loadSpotsData();
                JOptionPane.showMessageDialog(this, 
                    "Spot added successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid spot ID!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (UnauthorizedException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void removeParkingSpot() {
        int selectedRow = spotsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a spot to remove!", 
                "Warning", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int spotId = (int) spotsModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to remove spot " + spotId + "?", 
            "Confirm", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                parkingService.removeSpot(spotId);
                loadSpotsData();
                JOptionPane.showMessageDialog(this, 
                    "Spot removed successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (UnauthorizedException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadUsersData() {
        usersModel.setRowCount(0);
        try {
            List<User> users = admin.getAllOperators();
            for (User user : users) {
                Object[] row = {
                    user.getId(),
                    user.getName(),
                    user.getRole()
                };
                usersModel.addRow(row);
            }
        } catch (UnauthorizedException e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addUser() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JPasswordField passField = new JPasswordField();
        String[] roles = {"Entry Operator", "Exit Operator"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        
        panel.add(new JLabel("User ID:"));
        panel.add(idField);
        panel.add(new JLabel("Username:"));
        panel.add(nameField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Add New User", JOptionPane.OK_CANCEL_OPTION);
            
        if (result == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText());
                String name = nameField.getText().trim();
                String password = new String(passField.getPassword());
                String selectedRole = (String) roleBox.getSelectedItem();
                
                if (name.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Please fill all fields!", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                User newUser;
                if (selectedRole.equals("Entry Operator")) {
                    newUser = new EntryOperator(id, name, password);
                } else {
                    newUser = new ExitOperator(id, name, password);
                }
                
                admin.addOperator(newUser);
                loadUsersData();
                JOptionPane.showMessageDialog(this, 
                    "User added successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid ID!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (UnauthorizedException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a user to delete!", 
                "Warning", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int userId = (int) usersModel.getValueAt(selectedRow, 0);
        String userName = (String) usersModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete user: " + userName + "?", 
            "Confirm", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                List<User> users = admin.getAllOperators();
                User toDelete = null;
                for (User u : users) {
                    if (u.getId() == userId) {
                        toDelete = u;
                        break;
                    }
                }
                
                if (toDelete != null) {
                    admin.deleteOperator(toDelete);
                    loadUsersData();
                    JOptionPane.showMessageDialog(this, 
                        "User deleted successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (UnauthorizedException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteTicket() {
        int selectedRow = ticketsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a ticket to delete!", 
                "Warning", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int ticketId = (int) ticketsModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete ticket #" + ticketId + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                parkingService.deleteTicket(ticketId);
                loadTicketsData();
                JOptionPane.showMessageDialog(this, 
                    "Ticket deleted successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (UnauthorizedException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logout() {
        AuthorizationService.getInstance().logout();
        new LoginFrame(parkingService).setVisible(true);
        dispose();
    }
}
