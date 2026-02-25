package frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventorySystem extends JFrame {
    private final int managerId;
    private final String managerName;

    private DefaultTableModel inventoryModel;
    private JTable inventoryTable;

    private JTextField itemNameField;
    private JTextField quantityField;
    private JTextField reorderLevelField;

    private JButton addButton;
    private JButton updateSelectedButton;

    public InventorySystem(int managerId, String managerName) {
        this.managerId = managerId;
        this.managerName = managerName;

        setTitle("Boba POS - Inventory System: " + managerName);
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        loadInventory();
    }

    // Top bar: Back, Menu/Price (placeholder), Logout
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton backButton = new JButton("Back to Analytics");
        backButton.addActionListener(e -> openManagerDashboard());
        leftPanel.add(backButton);

        JButton menuPriceButton = new JButton("Price/Menu Adjustments");
        menuPriceButton.addActionListener(e -> openPriceMenuAdjustments());
        leftPanel.add(menuPriceButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setBackground(new Color(108, 117, 125));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setOpaque(true);
        logoutButton.addActionListener(e -> handleLogout());

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(logoutButton, BorderLayout.EAST);

        return topBar;
    }

    // Main content: inventory table + add/update panel
    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        inventoryModel = new DefaultTableModel(
                new Object[] { "ID", "Item Name", "Quantity In Stock", "Reorder Level" },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(inventoryModel);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inventoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFieldsFromSelection();
            }
        });

        main.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(BorderFactory.createTitledBorder("Add / Update Inventory"));
        controls.setPreferredSize(new Dimension(330, 0));

        itemNameField = new JTextField();
        quantityField = new JTextField();
        reorderLevelField = new JTextField();

        controls.add(makeLabeledField("Item Name", itemNameField));
        controls.add(Box.createVerticalStrut(10));
        controls.add(makeLabeledField("Quantity In Stock", quantityField));
        controls.add(Box.createVerticalStrut(10));
        controls.add(makeLabeledField("Reorder Level", reorderLevelField));
        controls.add(Box.createVerticalStrut(15));

        addButton = new JButton("Add New Item");
        addButton.addActionListener(e -> addInventoryItem());
        controls.add(addButton);

        controls.add(Box.createVerticalStrut(10));

        updateSelectedButton = new JButton("Update Selected Item");
        updateSelectedButton.addActionListener(e -> updateSelectedItem());
        controls.add(updateSelectedButton);

        controls.add(Box.createVerticalStrut(10));

        JButton refreshButton = new JButton("Refresh Table");
        refreshButton.addActionListener(e -> loadInventory());
        controls.add(refreshButton);

        main.add(controls, BorderLayout.EAST);

        return main;
    }

    private JPanel makeLabeledField(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JLabel label = new JLabel(labelText);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    // Load inventory from DB into table
    private void loadInventory() {
        inventoryModel.setRowCount(0);

        String sql =
                "SELECT inventory_id, item_name, quantity_in_stock, reorder_level " +
                "FROM Inventory " +
                "ORDER BY item_name ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    inventoryModel.addRow(new Object[] {
                            rs.getInt("inventory_id"),
                            rs.getString("item_name"),
                            rs.getBigDecimal("quantity_in_stock"),
                            rs.getBigDecimal("reorder_level")
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading inventory.");
        }
    }

    // When selecting a row, fill the form fields
    private void populateFieldsFromSelection() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            return;
        }

        String itemName = inventoryModel.getValueAt(row, 1).toString();
        String quantity = inventoryModel.getValueAt(row, 2).toString();
        String reorder = inventoryModel.getValueAt(row, 3).toString();

        itemNameField.setText(itemName);
        quantityField.setText(quantity);
        reorderLevelField.setText(reorder);
    }

    // Add new inventory item
    private void addInventoryItem() {
        String itemName = itemNameField.getText().trim();
        String quantityStr = quantityField.getText().trim();
        String reorderStr = reorderLevelField.getText().trim();

        if (itemName.isEmpty() || quantityStr.isEmpty() || reorderStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill Item Name, Quantity, and Reorder Level.");
            return;
        }

        BigDecimal quantity;
        BigDecimal reorder;
        try {
            quantity = new BigDecimal(quantityStr);
            reorder = new BigDecimal(reorderStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Reorder Level must be numeric.");
            return;
        }

        if (quantity.compareTo(BigDecimal.ZERO) < 0 || reorder.compareTo(BigDecimal.ZERO) < 0) {
            JOptionPane.showMessageDialog(this, "Quantity and Reorder Level must be >= 0.");
            return;
        }

        String sql =
                "INSERT INTO Inventory(item_name, quantity_in_stock, reorder_level) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            pstmt.setString(1, itemName);
            pstmt.setBigDecimal(2, quantity);
            pstmt.setBigDecimal(3, reorder);

            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Inventory item added!");
            clearFields();
            loadInventory();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding item (name must be unique).");
        }
    }

    // Update selected inventory item (quantity + reorder level)
    private void updateSelectedItem() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item in the table first.");
            return;
        }

        int inventoryId = (int) inventoryModel.getValueAt(row, 0);

        String quantityStr = quantityField.getText().trim();
        String reorderStr = reorderLevelField.getText().trim();

        if (quantityStr.isEmpty() || reorderStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill Quantity and Reorder Level.");
            return;
        }

        BigDecimal quantity;
        BigDecimal reorder;
        try {
            quantity = new BigDecimal(quantityStr);
            reorder = new BigDecimal(reorderStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Reorder Level must be numeric.");
            return;
        }

        if (quantity.compareTo(BigDecimal.ZERO) < 0 || reorder.compareTo(BigDecimal.ZERO) < 0) {
            JOptionPane.showMessageDialog(this, "Quantity and Reorder Level must be >= 0.");
            return;
        }

        String sql =
                "UPDATE Inventory " +
                "SET quantity_in_stock = ?, reorder_level = ? " +
                "WHERE inventory_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            pstmt.setBigDecimal(1, quantity);
            pstmt.setBigDecimal(2, reorder);
            pstmt.setInt(3, inventoryId);

            int updated = pstmt.executeUpdate();
            if (updated == 1) {
                JOptionPane.showMessageDialog(this, "Inventory item updated!");
                loadInventory();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed (item not found).");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating item.");
        }
    }

    private void clearFields() {
        itemNameField.setText("");
        quantityField.setText("");
        reorderLevelField.setText("");
    }

    private void openManagerDashboard() {
        dispose();
        new ManagerDashboard(managerId, managerName).setVisible(true);
    }

    private void openPriceMenuAdjustments() {
        dispose();
        new PriceMenuAdjustments(managerId, managerName).setVisible(true);
    }

    private void handleLogout() {
        dispose();
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setVisible(true);
    }
}