package frontend;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DrinkCustomization extends JDialog {
    private String drinkName;
    private double basePrice;
    private CashierDashboard parentDashboard;
    
    private ArrayList<JCheckBox> addonCheckboxes = new ArrayList<>();
    private ArrayList<Double> addonPrices = new ArrayList<>();

    // Builds the customization dialog for a selected drink.
    public DrinkCustomization(CashierDashboard parent, String drinkName, double basePrice) {
        super(parent, "Customize: " + drinkName, true);
        this.parentDashboard = parent;
        this.drinkName = drinkName;
        this.basePrice = basePrice;

        setSize(400, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JLabel headerLabel = new JLabel("Customizing: " + drinkName + " ($" + String.format("%.2f", basePrice) + ")", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(headerLabel, BorderLayout.NORTH);

        JPanel addonsPanel = new JPanel();
        addonsPanel.setLayout(new GridLayout(0, 1, 5, 5));
        addonsPanel.setBorder(BorderFactory.createTitledBorder("Select Add-ons"));
        loadAddons(addonsPanel);
        
        add(new JScrollPane(addonsPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton addButton = new JButton("Add to Order");

        cancelButton.addActionListener(e -> dispose());
        
        addButton.addActionListener(e -> processCustomizedDrink());

        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Loads add-on items and displays them as checkboxes.
    private void loadAddons(JPanel panel) {
        String sql = "SELECT item_name, base_price FROM Menu_Items WHERE item_type = 'Addon'";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String addonName = rs.getString("item_name");
                double price = rs.getDouble("base_price");

                JCheckBox checkBox = new JCheckBox(addonName + " (+$" + String.format("%.2f", price) + ")");
                checkBox.setFont(new Font("Arial", Font.PLAIN, 16));
                
                addonCheckboxes.add(checkBox);
                addonPrices.add(price);
                panel.add(checkBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load add-ons: " + e.getMessage());
        }
    }

    // Calculates the selected add-ons and reports them to the parent dashboard.
    private void processCustomizedDrink() {
        double totalItemPrice = basePrice;
        ArrayList<String> selectedAddons = new ArrayList<>();

        for (int i = 0; i < addonCheckboxes.size(); i++) {
            if (addonCheckboxes.get(i).isSelected()) {
                totalItemPrice += addonPrices.get(i);
                String cleanName = addonCheckboxes.get(i).getText().split(" \\(\\+\\$")[0];
                selectedAddons.add(cleanName);
            }
        }

        parentDashboard.addCustomizedItemToCart(drinkName, selectedAddons, totalItemPrice);
        dispose();
    }

}