package dojo.liftpasspricing;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HolidaysProvider implements DatabaseUtilities {
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

     @Override
     public List<Date> getHolidays() throws SQLException {
        List<Date> holidays = new ArrayList<>();
        try (PreparedStatement holidayStmt = getConnection().prepareStatement( // #2 - 110 -- DB stuff - isolate for testing
                "SELECT * FROM holidays")) {
            try (ResultSet holidaysResultSet = holidayStmt.executeQuery()) {

                // #1 - init

                //  Database logic
                while (holidaysResultSet.next()) {
                    Date holiday = holidaysResultSet.getDate("holiday");
                    holidays.add(holiday);
                }
            }
        }
        return holidays;
    }

    int getCostForLiftTicketType(String liftTicketType) throws SQLException {
        int costForLiftTicketTypeFromDatabase;
        try (PreparedStatement costStmt = getConnection().prepareStatement( //
                "SELECT cost FROM base_price " + //
                "WHERE type = ?")) {
            costStmt.setString(1, liftTicketType);
            try (ResultSet result = costStmt.executeQuery()) {
                result.next();
                costForLiftTicketTypeFromDatabase = result.getInt("cost");
            }
        }
        return costForLiftTicketTypeFromDatabase;
    }
}
