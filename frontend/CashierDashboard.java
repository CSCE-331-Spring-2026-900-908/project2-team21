package frontend;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CashierDashboard extends JFrame {
    private JPanel menuPanel;
    private DefaultListModel<String> cartModel;
    private JList<String> cartList;
    private JLabel totalLabel;
    
    private double currentTotal = 0.0;
    private int currentEmployeeId;
    
    private ArrayList<OrderItem> currentOrderItems = new ArrayList<>();
    private ArrayList<Integer> cartToOrderMap = new ArrayList<>();

    private class OrderItem {
        String drinkName;
        ArrayList<String> addons;
        double totalItemPrice;

        // Holds a single order item with its add-ons and price.
        OrderItem(String drinkName, ArrayList<String> addons, double totalItemPrice) {
            this.drinkName = drinkName;
            this.addons = addons;
            this.totalItemPrice = totalItemPrice;
        }
    }

    // Builds the cashier dashboard for a logged-in employee.
    public CashierDashboard(int employeeId, String employeeName) {
        this.currentEmployeeId = employeeId;
        
        setTitle("Boba POS - Cashier: " + employeeName);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // TOP PANEL (LOGOUT)
        JPanel topPanel = new JPanel(new BorderLayout());

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setBackground(new Color(108, 117, 125));
        logoutButton.setForeground(Color.WHITE);

        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setOpaque(true);
        logoutButton.addActionListener(e -> handleLogout());

        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // LEFT PANEL (DRINK MENU)
        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(0, 4, 10, 10)); 
        menuPanel.setBorder(BorderFactory.createTitledBorder("Drinks"));
        
        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // RIGHT PANEL (FINAL CART)
        JPanel cartPanel = new JPanel();
        cartPanel.setLayout(new BorderLayout(5, 5));
        cartPanel.setPreferredSize(new Dimension(300, 0));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Current Order"));

        cartModel = new DefaultListModel<>();
        cartList = new JList<>(cartModel);
        cartList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        cartPanel.add(new JScrollPane(cartList), BorderLayout.CENTER);

        // CHECKOUT SECTION
        JPanel checkoutPanel = new JPanel(new GridLayout(3, 1, 5, 5)); 
        
        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        checkoutPanel.add(totalLabel);

        JButton removeButton = new JButton("Remove Selected Item");
        removeButton.setBackground(new Color(220, 53, 69));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFont(new Font("Arial", Font.BOLD, 14));
        removeButton.addActionListener(e -> removeSelectedItem());
        checkoutPanel.add(removeButton);

        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.setBackground(new Color(60, 179, 113)); 
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 18));
        checkoutButton.addActionListener(e -> processCheckout());
        checkoutPanel.add(checkoutButton);

        cartPanel.add(checkoutPanel, BorderLayout.SOUTH);
        add(cartPanel, BorderLayout.EAST);

        loadDrinks();
    }

    // Handles logout and returns to login screen
    private void handleLogout() {
        dispose();
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setVisible(true);
    }

    // Loads drink menu items and renders them as buttons.
    private void loadDrinks() {
        // Clear existing buttons
        menuPanel.removeAll();
        
        // Modified SQL to include is_seasonal flag - order seasonal items first
        String sql = "SELECT item_name, base_price, COALESCE(is_seasonal, false) as is_seasonal " +
                    "FROM Menu_Items WHERE item_type = 'Drink' " +
                    "ORDER BY is_seasonal DESC, item_name ASC";
        
        try (Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String itemName = rs.getString("item_name");
                double price = rs.getDouble("base_price");
                boolean isSeasonal = rs.getBoolean("is_seasonal");

                JButton drinkButton = new JButton();
                drinkButton.setPreferredSize(new Dimension(150, 100));
                drinkButton.setFocusPainted(false);
                
                if (isSeasonal) {
                    // 🌟 SEASONAL ITEM STYLING 🌟
                    drinkButton.setBackground(new Color(255, 215, 0)); // Gold
                    drinkButton.setForeground(new Color(139, 0, 0)); // Dark red text
                    drinkButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 140, 0), 3), // Orange border
                        BorderFactory.createRaisedBevelBorder()
                    ));
                    
                    // Add stars and seasonal emoji
                    drinkButton.setText("<html><center>🌟 " + itemName + " 🌟<br><font color='#8B0000'>$" + 
                                    String.format("%.2f", price) + " ⏳</font></center></html>");
                    
                    // Add tooltip
                    drinkButton.setToolTipText("🌟 SEASONAL SPECIAL - Limited time only! 🌟");
                    
                } else {
                    // REGULAR ITEM STYLING
                    drinkButton.setBackground(new Color(173, 216, 230)); // Light blue
                    drinkButton.setForeground(Color.BLACK);
                    drinkButton.setBorder(BorderFactory.createRaisedBevelBorder());
                    drinkButton.setText("<html><center>" + itemName + "<br>$" + 
                                    String.format("%.2f", price) + "</center></html>");
                }
                
                // Add action listener
                drinkButton.addActionListener(e -> {
                    DrinkCustomization customizationPage = new DrinkCustomization(CashierDashboard.this, itemName, price);
                    customizationPage.setVisible(true);
                });

                menuPanel.add(drinkButton);
            }
            
            // If no seasonal items were found, add a note
            boolean hasSeasonal = false;
            rs.beforeFirst(); // Reset cursor
            while (rs.next()) {
                if (rs.getBoolean("is_seasonal")) {
                    hasSeasonal = true;
                    break;
                }
            }
            
            if (!hasSeasonal) {
                JLabel noSeasonal = new JLabel("No seasonal items currently available", SwingConstants.CENTER);
                noSeasonal.setForeground(new Color(255, 140, 0));
                noSeasonal.setFont(new Font("Arial", Font.ITALIC, 12));
                noSeasonal.setPreferredSize(new Dimension(150, 50));
                menuPanel.add(noSeasonal);
            }
            
            menuPanel.revalidate();
            menuPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load menu items: " + e.getMessage());
        }
    }

    // Adds a fully customized item to the current order.
    public void addCustomizedItemToCart(String drinkName, ArrayList<String> addons, double totalItemPrice) {
        currentOrderItems.add(new OrderItem(drinkName, addons, totalItemPrice));
        refreshCartUI();
    }

    // Removes the selected item from the cart list.
    private void removeSelectedItem() {
        int selectedIndex = cartList.getSelectedIndex();
        
        if (selectedIndex != -1) {
            int orderItemIndex = cartToOrderMap.get(selectedIndex);
            currentOrderItems.remove(orderItemIndex);

            refreshCartUI();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item in the cart to remove.");
        }
    }

    // Rebuilds the cart list and total based on current items.
    private void refreshCartUI() {
        cartModel.clear();
        cartToOrderMap.clear();
        currentTotal = 0.0;

        for (int i = 0; i < currentOrderItems.size(); i++) {
            OrderItem item = currentOrderItems.get(i);
            
            cartModel.addElement(item.drinkName + " - $" + String.format("%.2f", item.totalItemPrice));
            cartToOrderMap.add(i);
            
            for (String addon : item.addons) {
                cartModel.addElement("   + " + addon);
                cartToOrderMap.add(i);
            }
            
            currentTotal += item.totalItemPrice;
        }

        totalLabel.setText("Total: $" + String.format("%.2f", currentTotal));
    }

    // Persists the current order and its line items to the database.
    private void processCheckout() {
        if (currentOrderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The cart is empty!");
            return;
        }

        String insertOrderSql = "INSERT INTO Orders (employee_id, order_timestamp, total_amount) VALUES (?, ?, ?)";
        String getMenuIdSql = "SELECT menu_item_id FROM Menu_Items WHERE item_name = ?";
        String insertLineItemSql = "INSERT INTO Order_Line_Items (order_id, menu_item_id, quantity, sale_price) VALUES (?, ?, ?, ?)";
        String insertAddonSql = "INSERT INTO Line_Item_Add_Ons (line_item_id, add_on_menu_item_id, quantity) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection()) {
            if (conn == null) {
                return;
            }

            conn.setAutoCommit(false); 

            try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement pstmtGetMenuId = conn.prepareStatement(getMenuIdSql);
                 PreparedStatement pstmtLineItem = conn.prepareStatement(insertLineItemSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement pstmtAddon = conn.prepareStatement(insertAddonSql)) {

                pstmtOrder.setInt(1, currentEmployeeId);
                pstmtOrder.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
                pstmtOrder.setDouble(3, currentTotal);
                pstmtOrder.executeUpdate();

                int generatedOrderId = -1;
                try (ResultSet rsOrder = pstmtOrder.getGeneratedKeys()) {
                    if (rsOrder.next()) {
                        generatedOrderId = rsOrder.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated order_id.");
                    }
                }

                for (OrderItem item : currentOrderItems) {
                    pstmtGetMenuId.setString(1, item.drinkName);
                    int drinkMenuId = -1;
                    try (ResultSet rsDrinkId = pstmtGetMenuId.executeQuery()) {
                        if (rsDrinkId.next()) drinkMenuId = rsDrinkId.getInt("menu_item_id");
                    }

                    pstmtLineItem.setInt(1, generatedOrderId);
                    pstmtLineItem.setInt(2, drinkMenuId);
                    pstmtLineItem.setInt(3, 1);
                    pstmtLineItem.setDouble(4, item.totalItemPrice);
                    pstmtLineItem.executeUpdate();

                    int generatedLineItemId = -1;
                    try (ResultSet rsLineItem = pstmtLineItem.getGeneratedKeys()) {
                        if (rsLineItem.next()) generatedLineItemId = rsLineItem.getInt(1);
                    }

                    for (String addonName : item.addons) {
                        pstmtGetMenuId.setString(1, addonName);
                        int addonMenuId = -1;
                        try (ResultSet rsAddonId = pstmtGetMenuId.executeQuery()) {
                            if (rsAddonId.next()) addonMenuId = rsAddonId.getInt("menu_item_id");
                        }

                        pstmtAddon.setInt(1, generatedLineItemId);
                        pstmtAddon.setInt(2, addonMenuId);
                        pstmtAddon.setInt(3, 1);
                        pstmtAddon.executeUpdate();
                    }
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Order placed successfully! Order ID: " + generatedOrderId);

                currentOrderItems.clear();
                refreshCartUI();

            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Transaction failed and rolled back. Check console.");
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error during checkout.");
        }
    }
}