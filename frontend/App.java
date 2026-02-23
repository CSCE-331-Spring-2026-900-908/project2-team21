package frontend;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {                      
            System.out.println("Launching Boba POS System...");
            LoginScreen login = new LoginScreen();
            login.setVisible(true);
        });
    }
}