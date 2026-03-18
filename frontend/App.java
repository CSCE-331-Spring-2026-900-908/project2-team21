package frontend;

import javax.swing.SwingUtilities;

/**
 * The main entry point for the Boba POS System application.
 * Initializes the Swing Event Dispatch Thread and launches the login screen.
 * * @author Team 21
 * @version 1.0
 */
public class App {
    
    /**
     * Main method that launches the UI on the Swing event dispatch thread.
     * * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Launching Boba POS System...");
            LoginScreen login = new LoginScreen();
            login.setVisible(true);
        });
    }
}