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

}