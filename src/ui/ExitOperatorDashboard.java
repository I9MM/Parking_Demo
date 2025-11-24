package ui;

import model.ExitOperator;
import model.Ticket;
import security.AuthorizationService;
import service.ParkingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ExitOperatorDashboard extends JFrame {
    private ParkingService parkingService;
    private ExitOperator operator;
    private JTextField ticketIdField;
    private JTextArea detailsArea;
    private JTable invoicesTable;
    private DefaultTableModel invoicesModel;
    private JLabel totalAmountLabel;

    public ExitOperatorDashboard(ParkingService parkingService, ExitOperator operator) {
        this.parkingService = parkingService;
        this.operator = operator;
        initializeUI();
        loadInvoices();
    }

    private void initializeUI() {
        setTitle("Exit Operator Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(230, 126, 34));
        JLabel headerLabel = new JLabel("Exit Operator Dashboard");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 22));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main Panel with Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Process Exit", createProcessPanel());
        tabbedPane.addTab("All Invoices", createInvoicesPanel());
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
    
    private JPanel createProcessPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(BorderFactory.createTitledBorder("Process Exit"));
        inputPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        inputPanel.add(new JLabel("Ticket ID:"));
        ticketIdField = new JTextField(15);
        inputPanel.add(ticketIdField);

        JButton processButton = new JButton("Process Payment");
        processButton.setBackground(new Color(52, 152, 219));
        processButton.setForeground(Color.WHITE);
        processButton.addActionListener(e -> processExit());
        inputPanel.add(processButton);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Details Panel
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Ticket Details"));
        
        detailsArea = new JTextArea(15, 40);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        detailsArea.setBackground(new Color(236, 240, 241));
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        detailsPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createInvoicesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table
        String[] columns = {"Ticket ID", "Plate Number", "Owner", "Entry Time", "Amount (EGP)"};
        invoicesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        invoicesTable = new JTable(invoicesModel);
        invoicesTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(invoicesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with total
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalAmountLabel = new JLabel("Total Amount: 0.00 EGP");
        totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalAmountLabel.setForeground(new Color(39, 174, 96));
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadInvoices());
        
        bottomPanel.add(refreshButton);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(totalAmountLabel);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void processExit() {
        String ticketIdStr = ticketIdField.getText().trim();
        
        if (ticketIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a ticket ID!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int ticketId = Integer.parseInt(ticketIdStr);
            Ticket ticket = parkingService.findTicketById(ticketId);
            
            if (ticket != null) {
                double payment = operator.processPayment(ticket);
                
                StringBuilder details = new StringBuilder();
                details.append("========================================\n");
                details.append("           PARKING INVOICE\n");
                details.append("========================================\n\n");
                details.append("Ticket ID:      ").append(ticket.getEntryId()).append("\n");
                details.append("Plate Number:   ").append(ticket.getCar().getPlateNumber()).append("\n");
                details.append("Owner Name:     ").append(ticket.getCar().getOwnerName()).append("\n");
                details.append("National ID:    ").append(ticket.getCar().getNId()).append("\n");
                details.append("Entry Time:     ").append(ticket.getTimeIn().toString().substring(0, 16)).append("\n");
                details.append("\n----------------------------------------\n");
                details.append("PAYMENT AMOUNT: ").append(String.format("%.2f EGP", payment)).append("\n");
                details.append("----------------------------------------\n");
                
                detailsArea.setText(details.toString());
                
                JOptionPane.showMessageDialog(this, 
                    String.format("Payment: %.2f EGP\nThank you!", payment), 
                    "Payment Processed", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                ticketIdField.setText("");
                loadInvoices(); // Refresh invoices list
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Ticket not found!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                detailsArea.setText("");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid ticket ID!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadInvoices() {
        invoicesModel.setRowCount(0);
        double total = 0.0;
        
        try {
            List<Ticket> tickets = parkingService.getAllTicketsForExit();
            for (Ticket ticket : tickets) {
                double amount = ticket.getPayment();
                if (amount == 0) {
                    amount = ticket.calculatePayment();
                }
                
                Object[] row = {
                    ticket.getEntryId(),
                    ticket.getCar().getPlateNumber(),
                    ticket.getCar().getOwnerName(),
                    ticket.getTimeIn().toString().substring(0, 16),
                    String.format("%.2f", amount)
                };
                invoicesModel.addRow(row);
                total += amount;
            }
            
            totalAmountLabel.setText(String.format("Total Amount: %.2f EGP", total));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading invoices: " + e.getMessage(), 
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
