package bobapos;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Swing GUI components should run on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            System.out.println("Launching Boba POS System...");
            LoginScreen login = new LoginScreen();
            login.setVisible(true);
        });
    }
}