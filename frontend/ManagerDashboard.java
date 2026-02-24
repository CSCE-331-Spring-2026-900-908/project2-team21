package frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ManagerDashboard extends JFrame {
    private final int managerId;
    private final String managerName;

    private JComboBox<String> rangeDropdown;
    private JLabel totalSalesLabel;

    private DefaultTableModel salesByDayModel;
    private DefaultTableModel topItemsModel;
    private DefaultTableModel employeeSalesModel;

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

        rangeDropdown = new JComboBox<>(new String[] { "Last 7 Days", "Last 30 Days", "Last 365 Days", "All Time" });
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAnalytics());

        leftPanel.add(rangeDropdown);
        leftPanel.add(refreshButton);

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
        JTable employeeSalesTable = new JTable(employeeSalesModel);

        JTabbedPane rightTabs = new JTabbedPane();
        rightTabs.addTab("Top Items", new JScrollPane(topItemsTable));
        rightTabs.addTab("Employee Sales", new JScrollPane(employeeSalesTable));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(salesByDayTable),
                rightTabs
        );
        splitPane.setResizeWeight(0.55);

        main.add(splitPane, BorderLayout.CENTER);

        return main;
    }

    // Refresh all analytics based on selected date range
    private void refreshAnalytics() {
        Timestamp startTimestamp = getStartTimestampFromRange();

        loadTotalSales(startTimestamp);
        loadSalesByDay(startTimestamp);
        loadTopItems(startTimestamp);
        loadEmployeeSales(startTimestamp);
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
    
    // Inventory System view
    private void openInventorySystem() {
        dispose();
        new InventorySystem(managerId, managerName).setVisible(true);
    }
    
    //Price Adjustment view
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