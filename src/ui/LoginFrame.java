package ui;

import model.*;
import security.AuthorizationService;
import security.UnauthorizedException;
import service.ParkingService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private ParkingService parkingService;
    private AuthorizationService authService;

    public LoginFrame(ParkingService parkingService) {
        this.parkingService = parkingService;
        this.authService = AuthorizationService.getInstance();
        
        initializeUI();
        initializeSampleUsers();
    }

    private void initializeUI() {
        setTitle("Parking Management System - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 152, 219));
        JLabel headerLabel = new JLabel("Parking System Login");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("Username:"), gbc);
        
        usernameField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 0;
        centerPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(new JLabel("Password:"), gbc);
        
        passwordField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        centerPanel.add(passwordField, gbc);

        // Role
        gbc.gridx = 0; gbc.gridy = 2;
        centerPanel.add(new JLabel("Role:"), gbc);
        
        String[] roles = {"Admin", "Entry Operator", "Exit Operator"};
        roleComboBox = new JComboBox<>(roles);
        gbc.gridx = 1; gbc.gridy = 2;
        centerPanel.add(roleComboBox, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(46, 204, 113));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> handleLogin());
        
        JButton exitButton = new JButton("Exit");
        exitButton.setBackground(new Color(231, 76, 60));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void initializeSampleUsers() {
        // Initialize admin only if no users exist
        try {
            Admin admin = new Admin(1, "admin", "admin123");
            authService.login(admin);
            
            // Add initial parking spots only if none exist
            if (parkingService.getAllSpots().isEmpty()) {
                parkingService.addSpot(new ParkingSpot(101));
                parkingService.addSpot(new ParkingSpot(102));
                parkingService.addSpot(new ParkingSpot(103));
                parkingService.addSpot(new ParkingSpot(104));
                parkingService.addSpot(new ParkingSpot(105));
                System.out.println("Initial parking spots created");
            }
            
            authService.logout();
        } catch (UnauthorizedException e) {
            // Ignore for initialization
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String selectedRole = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter username and password!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simple authentication
        User user = authenticateUser(username, password, selectedRole);
        
        if (user != null) {
            authService.login(user);
            JOptionPane.showMessageDialog(this, 
                "Login successful! Welcome " + username, 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Open appropriate dashboard
            openDashboard(user);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid credentials or role!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    private User authenticateUser(String username, String password, String role) {
        // Authenticate from users.txt file
        repository.UserRepository userRepo = new repository.UserRepository();
        try {
            java.util.List<model.User> users = userRepo.loadAll();
            for (model.User user : users) {
                if (user.getName().equals(username) && 
                    user.getPassword().equals(password)) {
                    
                    // Check role matches
                    String expectedRole = "";
                    if (role.equals("Admin")) {
                        expectedRole = model.Role.ADMIN;
                    } else if (role.equals("Entry Operator")) {
                        expectedRole = model.Role.ENTRY_OPERATOR;
                    } else if (role.equals("Exit Operator")) {
                        expectedRole = model.Role.EXIT_OPERATOR;
                    }
                    
                    if (user.getRole().equals(expectedRole)) {
                        return user;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
        
        return null;
    }

    private void openDashboard(User user) {
        if (user instanceof Admin) {
            new AdminDashboard(parkingService).setVisible(true);
        } else if (user instanceof EntryOperator) {
            new EntryOperatorDashboard(parkingService, (EntryOperator) user).setVisible(true);
        } else if (user instanceof ExitOperator) {
            new ExitOperatorDashboard(parkingService, (ExitOperator) user).setVisible(true);
        }
    }
}
