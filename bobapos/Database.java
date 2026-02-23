package bobapos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu:5432/team_21_db";
    private static final String USER = "team_21";
    private static final String PASSWORD = "21";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return null; 
        }
    }

    public static void main(String[] args) {
        System.out.println("Attempting to connect to the AWS database...");
        Connection conn = getConnection();
        
        if (conn != null) {
            System.out.println("Success! Connected to team_21_db.");
            
            // Run a quick test query
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT first_name, last_name, role FROM Employees LIMIT 5;");
                
                System.out.println("\n--- Employee Test Query ---");
                while (rs.next()) {
                    System.out.println(rs.getString("first_name") + " " + 
                                       rs.getString("last_name") + " (" + 
                                       rs.getString("role") + ")");
                }
                conn.close();
            } catch (SQLException e) {
                System.err.println("Test query failed: " + e.getMessage());
            }
        }
    }
}