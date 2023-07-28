package dojo.liftpasspricing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseUtilities {
    Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/lift_pass", "root", "mysql");
    }

    public void setLiftPassCostForLiftPassType(int liftPassCost, String liftPassType) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement( //
                "INSERT INTO base_price (type, cost) VALUES (?, ?) " + //
                "ON DUPLICATE KEY UPDATE cost = ?")) {
            stmt.setString(1, liftPassType);
            stmt.setInt(2, liftPassCost);
            stmt.setInt(3, liftPassCost);
            stmt.execute();
        }
    }
}
