package frontend;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A modal dialog window that allows cashiers to select add-ons 
 * (like boba or jelly) for a specific drink before adding it to the cart.
 *
 * @author Team 21
 * @version 1.0
 */
public class DrinkCustomization extends JDialog {
    private String drinkName;
    private double basePrice;
    private CashierDashboard parentDashboard;
    
    private ArrayList<JCheckBox> addonCheckboxes = new ArrayList<>();
    private ArrayList<Double> addonPrices = new ArrayList<>();

    /**
     * Constructs the DrinkCustomization dialog.
     * Dynamically loads available add-ons from the database and calculates the new price.
     *
     * @param parent The CashierDashboard instance that opened this modal.
     * @param drinkName The name of the drink being customized.
     * @param basePrice The starting price of the drink before add-ons.
     */
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