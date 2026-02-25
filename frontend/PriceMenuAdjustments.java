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

}   