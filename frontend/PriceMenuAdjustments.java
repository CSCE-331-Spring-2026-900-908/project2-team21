package frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PriceMenuAdjustments extends JFrame {
    private final int managerId;
    private final String managerName;

    private JComboBox<String> filterDropdown;

    private DefaultTableModel menuModel;
    private JTable menuTable;

    private JTextField itemNameField;
    private JTextField basePriceField;
    private JComboBox<String> itemTypeDropdown;

    private DefaultTableModel orderHistoryModel;
    private JTable orderHistoryTable;

    private DefaultListModel<String> featuredModel;
    private JList<String> featuredList;

    private JLabel totalItemsValue;
    private JLabel avgPriceValue;
    private JLabel lastUpdatedValue;

    // Builds the price and menu adjustment screen for a manager.
    public PriceMenuAdjustments(int managerId, String managerName) {
        this.managerId = managerId;
        this.managerName = managerName;

        setTitle("Boba POS - Price/Menu Adjustments: " + managerName);
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        loadMenuItems();
        loadOrderHistoryAnalysis();
        loadFeaturedItems();
        loadMenuSummary();
    }

    // Top bar matches wireframe: Manage Employees (left), Logout (right), plus Back to Analytics
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton manageEmployeesButton = new JButton("Manage Employees");
        manageEmployeesButton.addActionListener(e -> openManageEmployees());
        leftPanel.add(manageEmployeesButton);

        JButton backButton = new JButton("Back to Analytics");
        backButton.addActionListener(e -> openManagerDashboard());
        leftPanel.add(backButton);

        filterDropdown = new JComboBox<>(new String[] { "All", "Drink", "Food", "Addon" });
        JButton applyFilterButton = new JButton("Apply Filter");
        applyFilterButton.addActionListener(e -> loadMenuItems());

        leftPanel.add(filterDropdown);
        leftPanel.add(applyFilterButton);

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

    // Main content layout similar to wireframe: left main area + right sidebar
    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JPanel leftArea = buildLeftArea();
        JPanel rightSidebar = buildRightSidebar();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftArea, rightSidebar);
        splitPane.setResizeWeight(0.70);
        main.add(splitPane, BorderLayout.CENTER);

        return main;
    }
    
    // Left area: chart placeholder + "Suggested Adjustments" (table + editor)
    private JPanel buildLeftArea() {
        JPanel left = new JPanel(new BorderLayout(10, 10));

        JPanel orderHistoryPanel = new JPanel(new BorderLayout(10, 10));
        orderHistoryPanel.setBorder(BorderFactory.createTitledBorder("Order History Analysis"));

        orderHistoryModel = new DefaultTableModel(new Object[] { "Item", "Units Sold", "Revenue" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderHistoryTable = new JTable(orderHistoryModel);
        orderHistoryPanel.add(new JScrollPane(orderHistoryTable), BorderLayout.CENTER);
        orderHistoryPanel.setPreferredSize(new Dimension(0, 230));

        left.add(orderHistoryPanel, BorderLayout.NORTH);

        // Table
        menuModel = new DefaultTableModel(new Object[] { "ID", "Item Name", "Type", "Base Price" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        menuTable = new JTable(menuModel);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFieldsFromSelection();
            }
        });

        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Suggested Adjustments / Menu Items"));
        tablePanel.add(new JScrollPane(menuTable), BorderLayout.CENTER);

        // Editor row (add/update)
        JPanel editorPanel = new JPanel(new GridLayout(2, 4, 10, 8));
        editorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        itemNameField = new JTextField();
        basePriceField = new JTextField();
        itemTypeDropdown = new JComboBox<>(new String[] { "Drink", "Food", "Addon" });

        JButton addButton = new JButton("Add New Item");
        addButton.addActionListener(e -> addMenuItem());

        JButton updateButton = new JButton("Update Selected");
        updateButton.addActionListener(e -> updateSelectedMenuItem());

        editorPanel.add(new JLabel("Item Name"));
        editorPanel.add(new JLabel("Type"));
        editorPanel.add(new JLabel("Base Price"));
        editorPanel.add(new JLabel(""));

        editorPanel.add(itemNameField);
        editorPanel.add(itemTypeDropdown);
        editorPanel.add(basePriceField);
        editorPanel.add(addButton);

        tablePanel.add(editorPanel, BorderLayout.SOUTH);

        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomButtons.add(updateButton);

        tablePanel.add(bottomButtons, BorderLayout.NORTH);

        left.add(tablePanel, BorderLayout.CENTER);

        return left;
    }

    // Right sidebar: Summary + Featured Items list (matches wireframe blocks)
    private JPanel buildRightSidebar() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(320, 0));

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        totalItemsValue = new JLabel("0");
        avgPriceValue = new JLabel("$0.00");
        lastUpdatedValue = new JLabel("—");

        summaryPanel.add(makeSummaryRow("Total Items:", totalItemsValue));
        summaryPanel.add(makeSummaryRow("Avg Price:", avgPriceValue));
        summaryPanel.add(makeSummaryRow("Last Updated:", lastUpdatedValue));

        featuredModel = new DefaultListModel<>();
        featuredList = new JList<>(featuredModel);

        JPanel featuredPanel = new JPanel(new BorderLayout());
        featuredPanel.setBorder(BorderFactory.createTitledBorder("Featured Items (Top Sellers)"));
        featuredPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        featuredPanel.add(new JScrollPane(featuredList), BorderLayout.CENTER);

        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(0, 10));

        right.add(summaryPanel);
        right.add(Box.createVerticalStrut(10));
        right.add(featuredPanel);
        right.add(Box.createVerticalGlue());

        return right;
    }

    // Creates a summary row with a label and value.
    private JPanel makeSummaryRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.add(new JLabel(label), BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    // Loads menu items from DB, optionally filtered by type
    private void loadMenuItems() {
        menuModel.setRowCount(0);

        String selectedType = (String) filterDropdown.getSelectedItem();
        boolean filtered = selectedType != null && !selectedType.equals("All");

        String sql =
                "SELECT menu_item_id, item_name, item_type, base_price " +
                "FROM Menu_Items " +
                (filtered ? "WHERE item_type = ? " : "") +
                "ORDER BY item_type ASC, item_name ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            if (filtered) {
                pstmt.setString(1, selectedType);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    menuModel.addRow(new Object[] {
                            rs.getInt("menu_item_id"),
                            rs.getString("item_name"),
                            rs.getString("item_type"),
                            rs.getBigDecimal("base_price")
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading menu items.");
        }
    }

    // Fill editor fields when selecting a table row
    private void populateFieldsFromSelection() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            return;
        }

        itemNameField.setText(menuModel.getValueAt(row, 1).toString());
        itemTypeDropdown.setSelectedItem(menuModel.getValueAt(row, 2).toString());
        basePriceField.setText(menuModel.getValueAt(row, 3).toString());
    }

    // Add menu item
    private void addMenuItem() {
        String itemName = itemNameField.getText().trim();
        String itemType = (String) itemTypeDropdown.getSelectedItem();
        String priceStr = basePriceField.getText().trim();

        if (itemName.isEmpty() || itemType == null || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill Item Name, Type, and Base Price.");
            return;
        }

        BigDecimal basePrice;
        try {
            basePrice = new BigDecimal(priceStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Base Price must be numeric.");
            return;
        }

        if (basePrice.compareTo(BigDecimal.ZERO) < 0) {
            JOptionPane.showMessageDialog(this, "Base Price must be >= 0.");
            return;
        }

        String sql =
                "INSERT INTO Menu_Items(item_name, item_type, base_price) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            pstmt.setString(1, itemName);
            pstmt.setString(2, itemType);
            pstmt.setBigDecimal(3, basePrice);

            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Menu item added!");
            clearFields();
            loadMenuItems();
            loadOrderHistoryAnalysis();
            loadFeaturedItems();
            loadMenuSummary();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding item (name may need to be unique).");
        }
    }

    // Update selected menu item price/type/name
    private void updateSelectedMenuItem() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a menu item first.");
            return;
        }

        int menuItemId = (int) menuModel.getValueAt(row, 0);

        String itemName = itemNameField.getText().trim();
        String itemType = (String) itemTypeDropdown.getSelectedItem();
        String priceStr = basePriceField.getText().trim();

        if (itemName.isEmpty() || itemType == null || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill Item Name, Type, and Base Price.");
            return;
        }

        BigDecimal basePrice;
        try {
            basePrice = new BigDecimal(priceStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Base Price must be numeric.");
            return;
        }

        if (basePrice.compareTo(BigDecimal.ZERO) < 0) {
            JOptionPane.showMessageDialog(this, "Base Price must be >= 0.");
            return;
        }

        String sql =
                "UPDATE Menu_Items " +
                "SET item_name = ?, item_type = ?, base_price = ? " +
                "WHERE menu_item_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            pstmt.setString(1, itemName);
            pstmt.setString(2, itemType);
            pstmt.setBigDecimal(3, basePrice);
            pstmt.setInt(4, menuItemId);

            int updated = pstmt.executeUpdate();
            if (updated == 1) {
                JOptionPane.showMessageDialog(this, "Menu item updated!");
                loadMenuItems();
                loadOrderHistoryAnalysis();
                loadFeaturedItems();
                loadMenuSummary();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed (item not found).");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating item.");
        }
    }

    // Clears the editor input fields.
    private void clearFields() {
        itemNameField.setText("");
        basePriceField.setText("");
        itemTypeDropdown.setSelectedIndex(0);
    }

    // Returns to the manager dashboard.
    private void openManagerDashboard() {
        dispose();
        new ManagerDashboard(managerId, managerName).setVisible(true);
    }

    // Opens the manage employees screen placeholder.
    private void openManageEmployees() {
        JOptionPane.showMessageDialog(this, "Manage Employees coming soon!");
    }

    // Logout back to login page
    private void handleLogout() {
        dispose();
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setVisible(true);
    }

    // Loads sales summary table (units + revenue) by item
    private void loadOrderHistoryAnalysis() {
        orderHistoryModel.setRowCount(0);

        String sql =
                "SELECT mi.item_name, " +
                "       COALESCE(SUM(oli.quantity), 0) AS units_sold, " +
                "       COALESCE(SUM(oli.quantity * oli.sale_price), 0) AS revenue " +
                "FROM Orders o " +
                "JOIN Order_Line_Items oli ON o.order_id = oli.order_id " +
                "JOIN Menu_Items mi ON mi.menu_item_id = oli.menu_item_id " +
                "GROUP BY mi.item_name " +
                "ORDER BY revenue DESC " +
                "LIMIT 15";

           try (Connection conn = Database.getConnection();
               PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                return;
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orderHistoryModel.addRow(new Object[] {
                            rs.getString("item_name"),
                            rs.getInt("units_sold"),
                            String.format("$%.2f", rs.getDouble("revenue"))
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}   