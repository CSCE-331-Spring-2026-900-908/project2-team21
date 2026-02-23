package frontend;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

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

    public CashierDashboard(int employeeId, String employeeName) {
        this.currentEmployeeId = employeeId;
        
        setTitle("Boba POS - Cashier: " + employeeName);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // LEFT PANEL (DRINK MENU)
        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(0, 4, 10, 10)); 
        menuPanel.setBorder(BorderFactory.createTitledBorder("Drinks"));
        
        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // RIGHT PANEL (FINAL CART)
        JPanel cartPanel = new JPanel();
        cartPanel.setLayout(new BorderLayout(5, 5));
        cartPanel.setPreferredSize(new Dimension(300, 0));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Current Order"));

        cartModel = new DefaultListModel<>();
        cartList = new JList<>(cartModel);
        cartList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        cartPanel.add(new JScrollPane(cartList), BorderLayout.CENTER);

        // CHECKOUT SECTION
        JPanel checkoutPanel = new JPanel(new GridLayout(3, 1, 5, 5)); 
        
        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        checkoutPanel.add(totalLabel);

        JButton removeButton = new JButton("Remove Selected Item");
        removeButton.setBackground(new Color(220, 53, 69)); // A nice red
        removeButton.setForeground(Color.WHITE);
        removeButton.setFont(new Font("Arial", Font.BOLD, 14));
        //removeButton.addActionListener(e -> //removeSelectedItem());
        checkoutPanel.add(removeButton);

        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.setBackground(new Color(60, 179, 113)); 
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 18));
        //checkoutButton.addActionListener(e -> //processCheckout());
        checkoutPanel.add(checkoutButton);

        cartPanel.add(checkoutPanel, BorderLayout.SOUTH);
        add(cartPanel, BorderLayout.EAST);

        //loadDrinks();
    }
}