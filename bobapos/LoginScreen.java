package bobapos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginScreen extends JFrame {

    public LoginScreen() {
        setTitle("Boba POS - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers the window on the screen

        // Main layout panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Title
        JLabel titleLabel = new JLabel("Login Page");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input Fields
        JTextField usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JPasswordField passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Role Selection (Radio Buttons)
        JRadioButton cashierBtn = new JRadioButton("Cashier");
        JRadioButton managerBtn = new JRadioButton("Manager");
        cashierBtn.setSelected(true); // Default selection
        
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(cashierBtn);
        roleGroup.add(managerBtn);

        JPanel rolePanel = new JPanel(new FlowLayout());
        rolePanel.add(cashierBtn);
        rolePanel.add(managerBtn);

        // Login Button
        JButton loginButton = new JButton("LOGIN");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBackground(Color.DARK_GRAY);
        loginButton.setForeground(Color.WHITE);

        // Button Action Logic
        loginButton.addActionListener((ActionEvent e) -> {
            boolean isManager = managerBtn.isSelected();

            // NOTE: In the next step, we will connect this to Database.java 
            // to actually verify the username/password against your Employees table!
            
            if (isManager) {
                JOptionPane.showMessageDialog(this, "Routing to Manager View...");
                // new ManagerScreen().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Routing to Cashier View...");
                // new CashierScreen().setVisible(true);
            }
            
            // Close the login window after navigating
            // this.dispose(); 
        });

        // Add components to the panel with spacing
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(rolePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(loginButton);

        add(panel);
    }
}