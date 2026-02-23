package frontend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu:5432/team_21_db";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, Credentials.username, Credentials.password);        // GET USER AND PASSWORD FROM CREDENTIALS FILE
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }
}