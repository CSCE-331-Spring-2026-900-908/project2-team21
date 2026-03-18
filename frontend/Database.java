package frontend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the JDBC connection to the PostgreSQL database.
 * This class establishes the link between the frontend GUI and the backend data.
 * * @author Team 21
 * @version 1.0
 */
public class Database {
    /** The JDBC URL pointing to the team database */
    private static final String URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu:5432/team_21_db";

    /**
     * Opens a new database connection using the configured credentials.
     * Retrieves the username and password from the git-ignored Credentials class.
     * * @return A valid Connection object if successful, or null if the connection fails.
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, Credentials.username, Credentials.password);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }
}