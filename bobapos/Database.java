package bobapos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
}