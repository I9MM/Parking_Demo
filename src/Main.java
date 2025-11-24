import service.ParkingService;
import ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch GUI
        SwingUtilities.invokeLater(() -> {
            ParkingService parkingService = new ParkingService();
            LoginFrame loginFrame = new LoginFrame(parkingService);
            loginFrame.setVisible(true);
        });
        
        System.out.println("=== Parking Management System Started ===");
        System.out.println("Login Credentials (from users.txt):");
        System.out.println("  Admin:           username=admin,           password=admin123");
        System.out.println("  Entry Operator:  username=mohamed_ali,     password=entry123");
        System.out.println("  Exit Operator:   username=sara_ahmed,      password=exit123");
        System.out.println("  Entry Operator:  username=khaled_hassan,   password=entry456");
        System.out.println("  Exit Operator:   username=fatma_mohamed,   password=exit456");
    }
}