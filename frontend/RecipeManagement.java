package frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class RecipeManagement extends JFrame {
    private final int managerId;
    private final String managerName;

    private DefaultTableModel recipeModel;
    private JTable recipeTable;

    private JComboBox<String> drinkDropdown;
    private JComboBox<String> ingredientDropdown;
    private JTextField quantityField;

    // Maps to hold names to IDs for easy database insertion
    private HashMap<String, Integer> drinkMap = new HashMap<>();
    private HashMap<String, Integer> inventoryMap = new HashMap<>();

    public RecipeManagement(int managerId, String managerName) {
        this.managerId = managerId;
        this.managerName = managerName;

        setTitle("Boba POS - Recipe Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Closes just this window
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top: Title
        JLabel titleLabel = new JLabel("Manage Recipes & Ingredients", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Center: Recipe Table
        recipeModel = new DefaultTableModel(new Object[]{"Menu Item (Drink/Addon)", "Ingredient", "Qty Used"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recipeTable = new JTable(recipeModel);
        add(new JScrollPane(recipeTable), BorderLayout.CENTER);

        // Bottom: Editing Form
        add(buildEditPanel(), BorderLayout.SOUTH);

        // Initial Data Load
        loadDropdownData();
        loadRecipeTable();
    }

    private JPanel buildEditPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Add / Update / Remove Recipe Link"));

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        
        drinkDropdown = new JComboBox<>();
        ingredientDropdown = new JComboBox<>();
        quantityField = new JTextField(5);

        formPanel.add(new JLabel("Menu Item:"));
        formPanel.add(drinkDropdown);
        formPanel.add(new JLabel("Ingredient:"));
        formPanel.add(ingredientDropdown);
        formPanel.add(new JLabel("Qty Used:"));
        formPanel.add(quantityField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("Save / Update Link");
        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveRecipeLink());

        JButton deleteButton = new JButton("Remove Ingredient");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deleteRecipeLink());

        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);

        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadDropdownData() {
        drinkMap.clear();
        inventoryMap.clear();
        drinkDropdown.removeAllItems();
        ingredientDropdown.removeAllItems();

        String menuSql = "SELECT menu_item_id, item_name FROM Menu_Items ORDER BY item_name";
        String invSql = "SELECT inventory_id, item_name FROM Inventory ORDER BY item_name";

        try (Connection conn = Database.getConnection()) {
            if (conn == null) return;

            // Load Menu Items
            try (PreparedStatement pstmt = conn.prepareStatement(menuSql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("item_name");
                    drinkMap.put(name, rs.getInt("menu_item_id"));
                    drinkDropdown.addItem(name);
                }
            }

            // Load Inventory Items
            try (PreparedStatement pstmt = conn.prepareStatement(invSql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("item_name");
                    inventoryMap.put(name, rs.getInt("inventory_id"));
                    ingredientDropdown.addItem(name);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load dropdown items.");
        }
    }

    private void loadRecipeTable() {
        recipeModel.setRowCount(0);

        String sql = 
            "SELECT m.item_name AS menu_item, i.item_name AS ingredient, r.quantity_used " +
            "FROM Recipes r " +
            "JOIN Menu_Items m ON r.menu_item_id = m.menu_item_id " +
            "JOIN Inventory i ON r.inventory_id = i.inventory_id " +
            "ORDER BY m.item_name, i.item_name";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) return;

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    recipeModel.addRow(new Object[]{
                            rs.getString("menu_item"),
                            rs.getString("ingredient"),
                            String.format("%.2f", rs.getDouble("quantity_used"))
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void saveRecipeLink() {
        String selectedDrink = (String) drinkDropdown.getSelectedItem();
        String selectedIngredient = (String) ingredientDropdown.getSelectedItem();
        String qtyStr = quantityField.getText().trim();

        if (selectedDrink == null || selectedIngredient == null || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select items and enter a quantity.");
            return;
        }

        try {
            double qty = Double.parseDouble(qtyStr);
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.");
                return;
            }

            int menuId = drinkMap.get(selectedDrink);
            int invId = inventoryMap.get(selectedIngredient);

            // Using ON CONFLICT to seamlessly insert a new link or update an existing one
            String sql = 
                "INSERT INTO Recipes (menu_item_id, inventory_id, quantity_used) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (menu_item_id, inventory_id) " +
                "DO UPDATE SET quantity_used = EXCLUDED.quantity_used";

            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

                if (conn == null || pstmt == null) return;

                pstmt.setInt(1, menuId);
                pstmt.setInt(2, invId);
                pstmt.setDouble(3, qty);

                pstmt.executeUpdate();
                loadRecipeTable();
                quantityField.setText("");
                JOptionPane.showMessageDialog(this, "Recipe updated successfully!");

            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid number.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error saving recipe.");
        }
    }

    private void deleteRecipeLink() {
        String selectedDrink = (String) drinkDropdown.getSelectedItem();
        String selectedIngredient = (String) ingredientDropdown.getSelectedItem();

        if (selectedDrink == null || selectedIngredient == null) return;

        int menuId = drinkMap.get(selectedDrink);
        int invId = inventoryMap.get(selectedIngredient);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Remove " + selectedIngredient + " from " + selectedDrink + "?", 
            "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Recipes WHERE menu_item_id = ? AND inventory_id = ?";

            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

                if (conn == null || pstmt == null) return;

                pstmt.setInt(1, menuId);
                pstmt.setInt(2, invId);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    loadRecipeTable();
                    JOptionPane.showMessageDialog(this, "Ingredient removed from recipe.");
                } else {
                    JOptionPane.showMessageDialog(this, "This ingredient is not linked to this menu item.");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}