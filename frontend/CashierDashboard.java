package frontend;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * The main point-of-sale interface for cashiers. 
 * Provides functionality to view the drink menu, customize orders with add-ons,
 * manage the current order ticket, and process transactions to the database.
 *
 * @author Team 21
 * @version 1.0
 */
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

        OrderItem(String drinkName, ArrayList<String> addons, double totalItemPrice) {
            this.drinkName = drinkName;
            this.addons = addons;
            this.totalItemPrice = totalItemPrice;
        }
    }

    /**
     * Constructs the CashierDashboard for a specific logged-in employee.
     * Initializes the dynamic menu grid and the interactive order cart.
     *
     * @param employeeId The unique database ID of the cashier.
     * @param employeeName The first name of the cashier for display purposes.
     */
    public CashierDashboard(int employeeId, String employeeName) {
        this.currentEmployeeId = employeeId;
        
        setTitle("Boba POS - Cashier: " + employeeName);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Left Panel: Dynamic Menu Grid ---
        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(0, 4, 10, 10)); 
        menuPanel.setBorder(BorderFactory.createTitledBorder("Drinks"));
        
        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // --- Right Panel: The Ticket/Cart ---
        JPanel cartPanel = new JPanel();
        cartPanel.setLayout(new BorderLayout(5, 5));
        cartPanel.setPreferredSize(new Dimension(300, 0));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Current Order"));

        cartModel = new DefaultListModel<>();
        cartList = new JList<>(cartModel);
        cartList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        cartPanel.add(new JScrollPane(cartList), BorderLayout.CENTER);

        // Checkout & Remove Section
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

    private void loadDrinks() {
        String sql = "SELECT item_name, base_price FROM Menu_Items WHERE item_type = 'Drink'";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String itemName = rs.getString("item_name");
                double price = rs.getDouble("base_price");

                JButton drinkButton = new JButton("<html><center>" + itemName + "<br>$" + String.format("%.2f", price) + "</center></html>");
                drinkButton.setPreferredSize(new Dimension(150, 100));
                drinkButton.setBackground(new Color(173, 216, 230)); 
                drinkButton.setFocusPainted(false);
                
                drinkButton.addActionListener(e -> {
                    DrinkCustomization customizationPage = new DrinkCustomization(CashierDashboard.this, itemName, price);
                    customizationPage.setVisible(true);
                });

                menuPanel.add(drinkButton);
            }
            
            menuPanel.revalidate();
            menuPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load menu items: " + e.getMessage());
        }
    }

    /**
     * Receives a customized drink from the DrinkCustomization modal and adds it
     * to both the backend order tracking list and the frontend GUI cart.
     *
     * @param drinkName The base name of the selected drink.
     * @param addons A list of names for the selected add-ons.
     * @param totalItemPrice The calculated price of the drink plus all add-ons.
     */
    public void addCustomizedItemToCart(String drinkName, ArrayList<String> addons, double totalItemPrice) {
        currentOrderItems.add(new OrderItem(drinkName, addons, totalItemPrice));
        refreshCartUI();
    }

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
            if (conn == null) return;

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