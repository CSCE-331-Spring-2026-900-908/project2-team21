package frontend;

import java.awt.*;
import javax.swing.*;

public class LoginScreen extends JFrame {
    private JTextField empIdField;
    private JButton loginButton;

    public LoginScreen() {
        setTitle("Boba POS - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Boba POS System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        JLabel empIdLabel = new JLabel("Employee ID:");
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(empIdLabel, gbc);

        empIdField = new JTextField(15);
        gbc.gridx = 1;
        add(empIdField, gbc);

        loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(loginButton, gbc);

        
    }
}