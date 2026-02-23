package frontend;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DrinkCustomization extends JDialog {
    private String drinkName;
    private double basePrice;
    private CashierDashboard parentDashboard;
    
    private ArrayList<JCheckBox> addonCheckboxes = new ArrayList<>();
    private ArrayList<Double> addonPrices = new ArrayList<>();

    public DrinkCustomization(CashierDashboard parent, String drinkName, double basePrice) {
        super(parent, "Customize: " + drinkName, true);
        this.parentDashboard = parent;
        this.drinkName = drinkName;
        this.basePrice = basePrice;

        setSize(400, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
    }

}