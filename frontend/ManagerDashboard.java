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
}