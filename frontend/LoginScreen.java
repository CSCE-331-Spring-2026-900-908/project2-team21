package frontend;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

        loginButton.addActionListener(e -> attemptLogin());
        empIdField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String empIdStr = empIdField.getText().trim();
        if (empIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Employee ID.");
            return;
        }

        try {
            int empId = Integer.parseInt(empIdStr);
            
            try (Connection conn = Database.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(this, "Database connection failed. Check console for details.");
                    return;
                }

                String sql = "SELECT first_name, role FROM Employees WHERE employee_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, empId);
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            String firstName = rs.getString("first_name");
                            String role = rs.getString("role");
                            
                            this.dispose();
                            
                            if (role.equals("Manager")) {
                                JOptionPane.showMessageDialog(this, "Manager Dashboard coming soon!");
                                // new ManagerDashboard(empId, firstName).setVisible(true);
                            } else {
                                // Pass BOTH the ID and Name to the Cashier Dashboard
                                new CashierDashboard(empId, firstName).setVisible(true);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid Employee ID. Please try again.");
                        }
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Employee ID must be a number.");
        } catch (SQLException ex) {
            ex.printStackTrace(); 
            JOptionPane.showMessageDialog(this, "Database error. Check terminal output.");
        }
    }
}