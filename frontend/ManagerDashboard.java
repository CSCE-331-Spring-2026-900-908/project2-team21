package frontend;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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

        // NEW: Seasonal Menu Item Box
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

    // NEW METHOD: Loads product inventory usage based on the selected time window
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
}