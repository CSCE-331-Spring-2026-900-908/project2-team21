import java.sql.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class DatabaseController {
    
    @FXML
    private Button queryButton; 
    
    @FXML
    private TextArea resultArea; 
    
    @FXML
    private Button closeButton; 
    
    // UPDATED: Pointing to Team 21's Database
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu:5432/team_21_db"; 
    
    @FXML
    public void initialize() {
        queryButton.setOnAction(event -> runQuery());
        closeButton.setOnAction(event -> closeWindow());
    }
    
    private void runQuery() {
        resultArea.setText("Query will run here...");

        try {
            dbSetup my = new dbSetup();
 
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            Statement stmt = conn.createStatement();

            // UPDATED: Querying your Employees table
            String sqlStatement = "SELECT first_name, last_name, role FROM Employees LIMIT 10;";
            ResultSet rs = stmt.executeQuery(sqlStatement);

            String result = "--- Employee Roster ---\n";
            while (rs.next()) {
                // UPDATED: Fetching the columns we actually created in Phase 2
                result += rs.getString("first_name") + " " + 
                          rs.getString("last_name") + " (" + 
                          rs.getString("role") + ")\n";
            }

            resultArea.setText(result);

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            resultArea.setText("Error connecting to database:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeWindow() { 
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}