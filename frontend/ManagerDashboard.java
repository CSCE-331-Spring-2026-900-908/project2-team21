package frontend;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * The management interface providing administrative controls and analytics.
 * Includes features for viewing sales reports, tracking product usage, 
 * managing employees, generating X/Z reports, and adding new seasonal items.
 *
 * @author Team 21
 * @version 1.0
 */
public class ManagerDashboard extends JFrame {
    private final int managerId;
    private final String managerName;

    private JComboBox<String> rangeDropdown;
    private JLabel totalSalesLabel;

    private DefaultTableModel salesByDayModel;
    private DefaultTableModel topItemsModel;
    private DefaultTableModel employeeSalesModel;
    private DefaultTableModel productUsageModel;

    // Employee management (inside Employee Sales tab)
    private DefaultTableModel employeesModel;
    private JTable employeesTable;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JComboBox<String> roleDropdown;

    private JButton addEmployeeButton;
    private JButton deleteEmployeeButton;

    /**
     * Constructs the ManagerDashboard for a specific logged-in manager.
     * Initializes the tabbed analytics views, employee management, and populates initial data.
     *
     * @param managerId The unique database ID of the manager.
     * @param managerName The name of the manager for display purposes.
     */
    public ManagerDashboard(int managerId, String managerName) {
        this.managerId = managerId;
        this.managerName = managerName;

        setTitle("Boba POS - Manager: " + managerName);
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        refreshAnalytics();
    }

    // Builds the top bar (Inventory + Logout + Date Range)
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton inventoryButton = new JButton("Inventory System");
        inventoryButton.addActionListener(e -> openInventorySystem());
        leftPanel.add(inventoryButton);

        JButton menuPriceButton = new JButton("Price/Menu Adjustments");
        menuPriceButton.addActionListener(e -> openPriceMenuAdjustments());
        leftPanel.add(menuPriceButton);

        // --- NEW: Recipe Management Button ---
        JButton recipeButton = new JButton("Recipe Management");
        recipeButton.addActionListener(e -> openRecipeManagement());
        leftPanel.add(recipeButton);
        // ---------------------------------------

        // Seasonal Menu Item Box
        JPanel seasonalBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        seasonalBox.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 83, 69), 2), // Thick red/orange border
            "NEW SEASONAL MENU ITEM",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(220, 83, 69)
        ));

        JButton addSeasonalBtn = new JButton("+ Add Seasonal Item & Inventory");
        addSeasonalBtn.setFont(new Font("Arial", Font.BOLD, 12));
        addSeasonalBtn.addActionListener(e -> launchSeasonalWizard());
        seasonalBox.add(addSeasonalBtn);

        leftPanel.add(seasonalBox);

        rangeDropdown = new JComboBox<>(new String[] { "Last 7 Days", "Last 30 Days", "Last 365 Days", "All Time" });
        rangeDropdown.addActionListener(e -> refreshAnalytics());

        leftPanel.add(rangeDropdown);

        // X-Report and Z-Report buttons
        JButton xReportButton = new JButton("Run X-Report");
        xReportButton.setBackground(new Color(23, 162, 184)); // Teal
        xReportButton.setForeground(Color.WHITE);
        xReportButton.addActionListener(e -> generateXReport());

        JButton zReportButton = new JButton("Run Z-Report");
        zReportButton.setBackground(new Color(220, 53, 69)); // Red
        zReportButton.setForeground(Color.WHITE);
        zReportButton.addActionListener(e -> generateZReport());

        leftPanel.add(xReportButton);
        leftPanel.add(zReportButton);

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

    // Builds the main dashboard layout
    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Summary row
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        JLabel summaryTitle = new JLabel("Sales Summary");
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 18));

        totalSalesLabel = new JLabel("Total: $0.00");
        totalSalesLabel.setFont(new Font("Arial", Font.BOLD, 18));

        summaryPanel.add(summaryTitle);
        summaryPanel.add(totalSalesLabel);

        main.add(summaryPanel, BorderLayout.NORTH);

        // Tables area
        salesByDayModel = new DefaultTableModel(new Object[] { "Date", "Orders", "Revenue" }, 0);
        JTable salesByDayTable = new JTable(salesByDayModel);

        topItemsModel = new DefaultTableModel(new Object[] { "Item", "Qty Sold", "Revenue" }, 0);
        JTable topItemsTable = new JTable(topItemsModel);

        employeeSalesModel = new DefaultTableModel(new Object[] { "Employee", "Orders", "Revenue" }, 0);
        
        // Initialize Product Usage table
        productUsageModel = new DefaultTableModel(new Object[] { "Inventory Item", "Total Quantity Used" }, 0);
        JTable productUsageTable = new JTable(productUsageModel);

        JTabbedPane rightTabs = new JTabbedPane();
        rightTabs.addTab("Sales Report", new JScrollPane(topItemsTable));
        rightTabs.addTab("Employee Sales", buildEmployeeSalesTab());
        rightTabs.addTab("Product Usage", new JScrollPane(productUsageTable));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(salesByDayTable),
                rightTabs
        );
        splitPane.setResizeWeight(0.55);

        main.add(splitPane, BorderLayout.CENTER);

        return main;
    }

    // Employee Sales tab: analytics table + add/delete employee section
    private JPanel buildEmployeeSalesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Employee sales analytics
        JTable employeeSalesTable = new JTable(employeeSalesModel);
        panel.add(new JScrollPane(employeeSalesTable), BorderLayout.CENTER);

        // Bottom: Add/Delete employees
        panel.add(buildEmployeeManagementPanel(), BorderLayout.SOUTH);

        // Initial load for employee list (delete section)
        loadEmployeesTable();

        return panel;
    }

    // Builds the Add/Delete employee panel shown under Employee Sales
    private JPanel buildEmployeeManagementPanel() {
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBorder(BorderFactory.createTitledBorder("Manage Employees"));

        employeesModel = new DefaultTableModel(new Object[] { "ID", "First Name", "Last Name", "Role" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeesTable = new JTable(employeesModel);
        employeesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScroll = new JScrollPane(employeesTable);
        tableScroll.setPreferredSize(new Dimension(900, 160));
        container.add(tableScroll, BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));

        firstNameField = new JTextField(10);
        lastNameField = new JTextField(10);
        roleDropdown = new JComboBox<>(new String[] { "Cashier", "Manager" });

        addEmployeeButton = new JButton("Add Employee");
        addEmployeeButton.addActionListener(e -> addEmployee());

        deleteEmployeeButton = new JButton("Delete Selected");
        deleteEmployeeButton.addActionListener(e -> deleteSelectedEmployee());

        form.add(new JLabel("First:"));
        form.add(firstNameField);
        form.add(new JLabel("Last:"));
        form.add(lastNameField);
        form.add(new JLabel("Role:"));
        form.add(roleDropdown);
        form.add(addEmployeeButton);
        form.add(deleteEmployeeButton);

        container.add(form, BorderLayout.SOUTH);

        return container;
    }

    // Refresh all analytics based on selected date range
    private void refreshAnalytics() {
        Timestamp startTimestamp = getStartTimestampFromRange();

        loadTotalSales(startTimestamp);
        loadSalesByDay(startTimestamp);
        loadTopItems(startTimestamp);
        loadEmployeeSales(startTimestamp);
        loadProductUsage(startTimestamp);

        // keep the employee list current too
        loadEmployeesTable();
    }

    // Date range -> start timestamp (or null for All Time)
    private Timestamp getStartTimestampFromRange() {
        String selected = (String) rangeDropdown.getSelectedItem();
        if (selected == null) {
            return Timestamp.valueOf(LocalDateTime.now().minusDays(7));
        }

        LocalDateTime start;
        switch (selected) {
            case "Last 7 Days":
                start = LocalDateTime.now().minusDays(7);
                return Timestamp.valueOf(start);
            case "Last 30 Days":
                start = LocalDateTime.now().minusDays(30);
                return Timestamp.valueOf(start);
            case "Last 365 Days":
                start = LocalDateTime.now().minusDays(365);
                return Timestamp.valueOf(start);
            case "All Time":
            default:
                return null;
        }
    }

    // Loads total sales into the summary label
    private void loadTotalSales(Timestamp startTimestamp) {
        String sql =
                "SELECT COALESCE(SUM(total_amount), 0) AS total_sales " +
                "FROM Orders " +
                (startTimestamp != null ? "WHERE order_timestamp >= ? " : "");

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            if (startTimestamp != null) {
                pstmt.setTimestamp(1, startTimestamp);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble("total_sales");
                    totalSalesLabel.setText(String.format("Total: $%.2f", total));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading total sales.");
        }
    }

    // Loads sales grouped by day (date, orders, revenue)
    private void loadSalesByDay(Timestamp startTimestamp) {
        salesByDayModel.setRowCount(0);

        String sql =
                "SELECT DATE(order_timestamp) AS day, COUNT(*) AS orders, COALESCE(SUM(total_amount), 0) AS revenue " +
                "FROM Orders " +
                (startTimestamp != null ? "WHERE order_timestamp >= ? " : "") +
                "GROUP BY day " +
                "ORDER BY day DESC " +
                "LIMIT 30";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                return;
            }

            if (startTimestamp != null) {
                pstmt.setTimestamp(1, startTimestamp);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    salesByDayModel.addRow(new Object[] {
                            rs.getDate("day"),
                            rs.getInt("orders"),
                            String.format("$%.2f", rs.getDouble("revenue"))
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Loads top selling drink items (qty + revenue)
    private void loadTopItems(Timestamp startTimestamp) {
        topItemsModel.setRowCount(0);

        String sql =
                "SELECT mi.item_name, SUM(oli.quantity) AS qty_sold, " +
                "       COALESCE(SUM(oli.sale_price * oli.quantity), 0) AS revenue " +
                "FROM Orders o " +
                "JOIN Order_Line_Items oli ON oli.order_id = o.order_id " +
                "JOIN Menu_Items mi ON mi.menu_item_id = oli.menu_item_id " +
                "WHERE mi.item_type = 'Drink' " +
                (startTimestamp != null ? "AND o.order_timestamp >= ? " : "") +
                "GROUP BY mi.item_name " +
                "ORDER BY revenue DESC " +
                "LIMIT 10";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                return;
            }

            if (startTimestamp != null) {
                pstmt.setTimestamp(1, startTimestamp);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    topItemsModel.addRow(new Object[] {
                            rs.getString("item_name"),
                            rs.getInt("qty_sold"),
                            String.format("$%.2f", rs.getDouble("revenue"))
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Loads employee sales (orders count + revenue)
    private void loadEmployeeSales(Timestamp startTimestamp) {
        employeeSalesModel.setRowCount(0);

        String sql =
                "SELECT (e.first_name || ' ' || e.last_name) AS employee_name, " +
                "       COUNT(*) AS orders, COALESCE(SUM(o.total_amount), 0) AS revenue " +
                "FROM Orders o " +
                "JOIN Employees e ON e.employee_id = o.employee_id " +
                (startTimestamp != null ? "WHERE o.order_timestamp >= ? " : "") +
                "GROUP BY employee_name " +
                "ORDER BY revenue DESC " +
                "LIMIT 15";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                return;
            }

            if (startTimestamp != null) {
                pstmt.setTimestamp(1, startTimestamp);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employeeSalesModel.addRow(new Object[] {
                            rs.getString("employee_name"),
                            rs.getInt("orders"),
                            String.format("$%.2f", rs.getDouble("revenue"))
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Loads product inventory usage based on the selected time window
    private void loadProductUsage(Timestamp startTimestamp) {
        productUsageModel.setRowCount(0);

        // Combines base drink items and add-on items sold, then calculates the inventory used
        String sql = 
            "WITH All_Items_Sold AS ( " +
            "    SELECT oli.menu_item_id, oli.quantity AS total_qty, o.order_timestamp " +
            "    FROM Order_Line_Items oli " +
            "    JOIN Orders o ON oli.order_id = o.order_id " +
            "    UNION ALL " +
            "    SELECT lia.add_on_menu_item_id AS menu_item_id, (oli.quantity * lia.quantity) AS total_qty, o.order_timestamp " +
            "    FROM Line_Item_Add_Ons lia " +
            "    JOIN Order_Line_Items oli ON lia.line_item_id = oli.line_item_id " +
            "    JOIN Orders o ON oli.order_id = o.order_id " +
            ") " +
            "SELECT i.item_name, SUM(ais.total_qty * r.quantity_used) AS total_inventory_used " +
            "FROM All_Items_Sold ais " +
            "JOIN Recipes r ON ais.menu_item_id = r.menu_item_id " +
            "JOIN Inventory i ON r.inventory_id = i.inventory_id " +
            (startTimestamp != null ? "WHERE ais.order_timestamp >= ? " : "") +
            "GROUP BY i.item_name " +
            "ORDER BY total_inventory_used DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) return;

            if (startTimestamp != null) {
                pstmt.setTimestamp(1, startTimestamp);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    productUsageModel.addRow(new Object[] {
                            rs.getString("item_name"),
                            String.format("%.2f", rs.getDouble("total_inventory_used")) // Formatted to 2 decimal places
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Failed to load product usage.");
        }
    }


    // Loads all employees into the employees table (for delete selection)
    private void loadEmployeesTable() {
        if (employeesModel == null) return;

        employeesModel.setRowCount(0);

        String sql =
                "SELECT employee_id, first_name, last_name, role " +
                "FROM Employees " +
                "ORDER BY employee_id ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                return;
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employeesModel.addRow(new Object[] {
                            rs.getInt("employee_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("role")
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading employees.");
        }
    }

    // Add employee (matches schema: role must be 'Cashier' or 'Manager')
    private void addEmployee() {
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String role = (String) roleDropdown.getSelectedItem();

        if (first.isEmpty() || last.isEmpty() || role == null) {
            JOptionPane.showMessageDialog(this, "Please enter first name, last name, and role.");
            return;
        }

        String sql = "INSERT INTO Employees (first_name, last_name, role) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            pstmt.setString(1, first);
            pstmt.setString(2, last);
            pstmt.setString(3, role);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                firstNameField.setText("");
                lastNameField.setText("");
                roleDropdown.setSelectedIndex(0);

                loadEmployeesTable();
                refreshAnalytics();

                JOptionPane.showMessageDialog(this, "Employee added.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding employee.");
        }
    }

    // Delete selected employee (may fail if employee is referenced by Orders)
    private void deleteSelectedEmployee() {
        if (employeesTable == null || employeesModel == null) return;

        int row = employeesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an employee to delete.");
            return;
        }

        int employeeId = (int) employeesModel.getValueAt(row, 0);
        String employeeName = employeesModel.getValueAt(row, 1) + " " + employeesModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete employee: " + employeeName + " (ID " + employeeId + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM Employees WHERE employee_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {

            if (conn == null || pstmt == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            pstmt.setInt(1, employeeId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                loadEmployeesTable();
                refreshAnalytics();
                JOptionPane.showMessageDialog(this, "Employee deleted.");
            } else {
                JOptionPane.showMessageDialog(this, "Employee not found.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Could not delete employee. They may be referenced by existing orders."
            );
        }
    }
    
    // Phase 4 Seasonal Menu Item Wizard
    private void launchSeasonalWizard() {
        JTextField drinkNameField = new JTextField(15);
        JTextField drinkPriceField = new JTextField(10);
        JTextField ingredientNameField = new JTextField(15);
        JTextField initialStockField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Seasonal Drink Name:"));
        panel.add(drinkNameField);
        panel.add(new JLabel("Drink Price ($):"));
        panel.add(drinkPriceField);
        panel.add(new JLabel("New Ingredient Name:"));
        panel.add(ingredientNameField);
        panel.add(new JLabel("Initial Stock Quantity:"));
        panel.add(initialStockField);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                 "Add New Seasonal Item & Inventory", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String drinkName = drinkNameField.getText().trim();
            String ingredientName = ingredientNameField.getText().trim();
            double drinkPrice;
            double initialStock;

            try {
                drinkPrice = Double.parseDouble(drinkPriceField.getText().trim());
                initialStock = Double.parseDouble(initialStockField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for Price and Stock.");
                return;
            }

            if (drinkName.isEmpty() || ingredientName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Names cannot be empty.");
                return;
            }

            // Execute the 3-step transaction
            String insertInvSql = "INSERT INTO Inventory (item_name, quantity_in_stock, reorder_level) VALUES (?, ?, 50.0)";
            String insertMenuSql = "INSERT INTO Menu_Items (item_name, base_price, item_type, is_seasonal) VALUES (?, ?, 'Drink', true)";
            String insertRecipeSql = "INSERT INTO Recipes (menu_item_id, inventory_id, quantity_used) VALUES (?, ?, 1.0)";

            try (Connection conn = Database.getConnection()) {
                if (conn == null) return;
                
                conn.setAutoCommit(false); // Start transaction

                try (PreparedStatement invStmt = conn.prepareStatement(insertInvSql, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement menuStmt = conn.prepareStatement(insertMenuSql, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement recipeStmt = conn.prepareStatement(insertRecipeSql)) {

                    // 1. Insert Inventory
                    invStmt.setString(1, ingredientName);
                    invStmt.setDouble(2, initialStock);
                    invStmt.executeUpdate();
                    
                    int invId = -1;
                    try (ResultSet rs = invStmt.getGeneratedKeys()) {
                        if (rs.next()) invId = rs.getInt(1);
                    }

                    // 2. Insert Menu Item
                    menuStmt.setString(1, drinkName);
                    menuStmt.setDouble(2, drinkPrice);
                    menuStmt.executeUpdate();

                    int menuId = -1;
                    try (ResultSet rs = menuStmt.getGeneratedKeys()) {
                        if (rs.next()) menuId = rs.getInt(1);
                    }

                    // 3. Link them in Recipes
                    recipeStmt.setInt(1, menuId);
                    recipeStmt.setInt(2, invId);
                    recipeStmt.executeUpdate();

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Success! Seasonal item '" + drinkName + "' and ingredient '" + ingredientName + "' added to POS.");
                    
                    // Refresh the Top Items / Product Usage charts just in case
                    refreshAnalytics();

                } catch (SQLException ex) {
                    conn.rollback();
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to add seasonal item. Ensure the names don't already exist in the database.");
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Opens the Recipe Management pop-up window
    private void openRecipeManagement() {
        // Notice we DO NOT call dispose() here, so the dashboard stays open in the background!
        new RecipeManagement(managerId, managerName).setVisible(true);
    }

    // Inventory System view
    private void openInventorySystem() {
        dispose();
        new InventorySystem(managerId, managerName).setVisible(true);
    }

    // Price Adjustment view
    private void openPriceMenuAdjustments() {
        dispose();
        new PriceMenuAdjustments(managerId, managerName).setVisible(true);
    }

    // Handles logout and returns to login screen
    private void handleLogout() {
        dispose();
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setVisible(true);
    }

    // The X-Report looks at today's orders that haven't been "closed" by a Z-Report yet
    private void generateXReport() {
        String sql = 
            "SELECT EXTRACT(HOUR FROM order_timestamp) AS hour_of_day, " +
            "       COUNT(order_id) AS orders_count, " +
            "       COALESCE(SUM(total_amount), 0) AS hour_sales " +
            "FROM Orders " +
            "WHERE DATE(order_timestamp) = CURRENT_DATE AND is_closed = FALSE " +
            "GROUP BY hour_of_day " +
            "ORDER BY hour_of_day";

        StringBuilder receipt = new StringBuilder();
        receipt.append("====================================\n");
        receipt.append("             X - REPORT             \n");
        receipt.append("====================================\n");
        receipt.append("Date: ").append(java.time.LocalDate.now()).append("\n");
        receipt.append("Manager: ").append(managerName).append("\n\n");
        receipt.append(String.format("%-10s %-10s %-10s\n", "HOUR", "ORDERS", "SALES"));
        receipt.append("------------------------------------\n");

        double totalSales = 0.0;
        int totalOrders = 0;

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int hour = rs.getInt("hour_of_day");
                int orders = rs.getInt("orders_count");
                double sales = rs.getDouble("hour_sales");
                
                // FIXED TIME FORMATTING
                String timeStr;
                if (hour == 0) {
                    timeStr = "12 AM";
                } else if (hour == 12) {
                    timeStr = "12 PM";
                } else if (hour > 12) {
                    timeStr = (hour - 12) + " PM";
                } else {
                    timeStr = hour + " AM";
                }

                receipt.append(String.format("%-10s %-10d $%-9.2f\n", timeStr, orders, sales));
                totalSales += sales;
                totalOrders += orders;
            }

            if (!hasData) {
                receipt.append("No open transactions for today.\n");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to generate X-Report.");
            return;
        }

        receipt.append("------------------------------------\n");
        receipt.append(String.format("TOTAL ORDERS: %d\n", totalOrders));
        receipt.append(String.format("GROSS SALES:  $%.2f\n", totalSales));
        receipt.append("====================================\n");
        receipt.append("      END OF X-REPORT READOUT       \n");

        showReceiptDialog("X-Report", receipt.toString());
    }

    // Z report looks at same things as x-report but at end of day and resets all values to 0 after for next day and next x report
    private void generateZReport() {
        
        String checkSql = "SELECT COUNT(*) FROM Z_Reports WHERE report_date = CURRENT_DATE";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             ResultSet checkRs = checkStmt.executeQuery()) {
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "A Z-Report has already been generated today. The drawer is closed.",
                    "Z-Report Blocked",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "WARNING: Generating the Z-Report will close out the drawer for the day and reset X-Report totals to zero.\n\nAre you sure you want to proceed?",
            "Confirm Z-Report",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;
        String gatherSql =
        "SELECT COUNT(order_id) AS total_orders, COALESCE(SUM(total_amount), 0) AS total_sales " +
        "FROM Orders WHERE DATE(order_timestamp) = CURRENT_DATE AND is_closed = FALSE";
        
        double daySales = 0.0;
        int dayOrders = 0;

        try (Connection conn = Database.getConnection();
             PreparedStatement gatherStmt = conn.prepareStatement(gatherSql);
             ResultSet gatherRs = gatherStmt.executeQuery()) {
            
            if (gatherRs.next()) {
                dayOrders = gatherRs.getInt("total_orders");
                daySales = gatherRs.getDouble("total_sales");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }
        String insertZReportSql =
            "INSERT INTO Z_Reports (report_date, total_sales, manager_id) VALUES (CURRENT_DATE, ?, ?)";
        String resetOrdersSql =
            "UPDATE Orders SET is_closed = TRUE WHERE DATE(order_timestamp) = CURRENT_DATE AND is_closed = FALSE";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement insertStmt = conn.prepareStatement(insertZReportSql);
                 PreparedStatement resetStmt = conn.prepareStatement(resetOrdersSql)) {

                insertStmt.setDouble(1, daySales);
                insertStmt.setInt(2, managerId);
                insertStmt.executeUpdate();

                resetStmt.executeUpdate();

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to execute Z-Report transaction.");
            return;
        }

        StringBuilder receipt = new StringBuilder();
        receipt.append("====================================\n");
        receipt.append("             Z - REPORT             \n");
        receipt.append("          END OF DAY CLOSE          \n");
        receipt.append("====================================\n");
        receipt.append("Date: ").append(java.time.LocalDate.now()).append("\n");
        receipt.append("Time: ").append(java.time.LocalTime.now().withNano(0)).append("\n");
        receipt.append("Manager ID: ").append(managerId).append("\n");
        receipt.append("Manager Name: ").append(managerName).append("\n\n");
        receipt.append("------------------------------------\n");
        receipt.append(String.format("TOTAL DAY ORDERS: %d\n", dayOrders));
        receipt.append(String.format("TOTAL DAY SALES:  $%.2f\n", daySales));
        receipt.append("------------------------------------\n");
        receipt.append("SYSTEM STATUS: \n");
        receipt.append("- ACTIVE ORDERS RESET TO ZERO\n");
        receipt.append("- Z-REPORT LOCKED FOR REMAINDER OF DAY\n\n");
        receipt.append("Manager Signature:\n\n\n");
        receipt.append("X___________________________________\n");
        receipt.append("====================================\n");

        showReceiptDialog("Z-Report", receipt.toString());
        
    }
    // A helper method to display the formatted text cleanly
    private void showReceiptDialog(String title, String receiptText) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        
        JTextArea textArea = new JTextArea(receiptText);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(false);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        dialog.add(new JScrollPane(textArea), BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}