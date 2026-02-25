package frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InventorySystem extends JFrame {

    private final int managerId;
    private final String managerName;

    private DefaultTableModel inventoryModel;
    private JTable inventoryTable;

    private JTextField itemNameField;
    private JTextField quantityField;
    private JTextField reorderLevelField;

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
    }

    // Top bar: Back, Menu/Price, Logout
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

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(logoutButton, BorderLayout.EAST);

        return topBar;
    }

    // Main content: inventory table + add/update panel
    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Inventory table
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

        main.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        // Right-side controls
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

        controls.add(new JButton("Add New Item"));
        controls.add(Box.createVerticalStrut(10));
        controls.add(new JButton("Update Selected Item"));
        controls.add(Box.createVerticalStrut(10));
        controls.add(new JButton("Refresh Table"));

        main.add(controls, BorderLayout.EAST);

        return main;
    }

    private JPanel makeLabeledField(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(labelText), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    // // Price Adjustment view
    private void openPriceMenuAdjustments() {
        dispose();
        new PriceMenuAdjustments(managerId, managerName).setVisible(true);
    }

    // Returns to the manager dashboard.
    private void openManagerDashboard() {
        dispose();
        new ManagerDashboard(managerId, managerName).setVisible(true);
    }
}