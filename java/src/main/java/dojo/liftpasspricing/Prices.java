package dojo.liftpasspricing;

import spark.Request;

import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.put;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Prices {

    public static Connection createApp() throws SQLException {

        final Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lift_pass", "root", "mysql");

        port(4567);

        put("/prices", (req, res) -> {
            int liftPassCost = Integer.parseInt(req.queryParams("cost"));
            String liftPassType = req.queryParams("type");

            try (PreparedStatement stmt = connection.prepareStatement( //
                    "INSERT INTO base_price (type, cost) VALUES (?, ?) " + //
                            "ON DUPLICATE KEY UPDATE cost = ?")) {
                stmt.setString(1, liftPassType);
                stmt.setInt(2, liftPassCost);
                stmt.setInt(3, liftPassCost);
                stmt.execute();
            }

            return "";
        });

        get("/prices", (req, res) -> {
            final Integer age = req.queryParams("age") != null ? Integer.valueOf(req.queryParams("age")) : null;

            try (PreparedStatement costStmt = connection.prepareStatement( //
                    "SELECT cost FROM base_price " + //
                            "WHERE type = ?")) {
                costStmt.setString(1, req.queryParams("type"));
                return banana_getObject(connection, req, age, costStmt);
            }
        });

        after((req, res) -> {
            res.type("application/json");
        });

        return connection;
    }

    private static Object banana_getObject(Connection connection, Request req, Integer age, PreparedStatement costStmt) throws SQLException, ParseException {
        try (ResultSet result = costStmt.executeQuery()) {
            result.next();

            boolean isHoliday = false;

            int reduction;
            if (age == null) {
                int reduction2;
                reduction2 = 0;

                if (isNight(req)) {
                    return "{ \"cost\": 0}";
                }
                DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

                String formDateAsIsoFormat = req.queryParams("date");
                try (PreparedStatement holidayStmt = connection.prepareStatement( //
                        "SELECT * FROM holidays")) {
                    try (ResultSet holidays = holidayStmt.executeQuery()) {

                        isHoliday = isAnyHoliday(isHoliday, isoFormat, formDateAsIsoFormat, holidays);

                    }
                }

                if (formDateAsIsoFormat == null) {
                } else {
                    if (isNonHolidayAndIsLowerCostDay(isHoliday, isoFormat, formDateAsIsoFormat)) {
                        int reduction1;
                        reduction1 = 35;
                        reduction = reduction1;
                    }
                }

                double cost;
                cost = getCost(result) * (1 - reduction / 100.0);
                return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
            }
            if (age < 6) {
                return "{ \"cost\": 0}";
            }
            reduction = 0;

            if (isNight(req)) {
                if (age > 64) {
                    return "{ \"cost\": " + (int) Math.ceil(getCost(result) * .4) + "}";
                } else {
                    return "{ \"cost\": " + getCost(result) + "}";
                }
            }
            DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

            String formDateAsIsoFormat = req.queryParams("date");
            try (PreparedStatement holidayStmt = connection.prepareStatement( //
                    "SELECT * FROM holidays")) {
                try (ResultSet holidays = holidayStmt.executeQuery()) {
                    isHoliday = isAnyHoliday(isHoliday, isoFormat, formDateAsIsoFormat, holidays);
                }
            }

            if (formDateAsIsoFormat == null) {
                return banana(age, result, reduction);
            } else {
                if (isNonHolidayAndIsLowerCostDay(isHoliday, isoFormat, formDateAsIsoFormat)) {
                    reduction = 35;
                }
                return banana(age, result, reduction);
            }

            // TODO apply reduction for others
        }
    }

    private static boolean isNight(Request req) {
        return req.queryParams("type").equals("night");
    }

    private static int getCost(ResultSet result) throws SQLException {
        return result.getInt("cost");
    }

    private static Object banana(Integer age, ResultSet result, int reduction) throws SQLException {
        int bananaCost;
        int costFromResultSet = getCost(result);
        double ageFactor = 1;
        if (age == null) {
            bananaCost = (int) Math.ceil(costFromResultSet * ageFactor * (1 - reduction / 100.0));
            return "{ \"cost\": " + bananaCost + "}";
        }
        if (age < 15) {
            ageFactor = .7;
            bananaCost = (int) Math.ceil(costFromResultSet * ageFactor);
            return "{ \"cost\": " + bananaCost + "}";
        }
        if (age > 64) {
            ageFactor = .75;
            bananaCost = (int) Math.ceil(costFromResultSet * ageFactor * (1 - reduction / 100.0));
            return "{ \"cost\": " + bananaCost + "}";
        } else {
            bananaCost = (int) Math.ceil(costFromResultSet * ageFactor * (1 - reduction / 100.0));
            return "{ \"cost\": " + bananaCost + "}";
        }
    }

    private static boolean isNonHolidayAndIsLowerCostDay(boolean isHoliday, DateFormat isoFormat, String formDateAsIsoFormat) throws ParseException {
        return !isHoliday && isLowerCostDay(isoFormat, formDateAsIsoFormat);
    }

    private static boolean isLowerCostDay(DateFormat isoFormat, String formDateAsIsoFormat) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(isoFormat.parse(formDateAsIsoFormat));
        return calendar.get(Calendar.DAY_OF_WEEK) == 2;
    }

    private static boolean isAnyHoliday(boolean isHoliday, DateFormat isoFormat, String formDateAsIsoFormat, ResultSet holidayResultSet) throws SQLException, ParseException {
        while (holidayResultSet.next()) {
            Date holiday = holidayResultSet.getDate("holiday");
            if (formDateAsIsoFormat != null) {
                Date form_date = isoFormat.parse(formDateAsIsoFormat);
                if (isSpecificHoliday(holiday, form_date)) {
                    isHoliday = true;
                }
            }
        }
        return isHoliday;
    }

    private static boolean isSpecificHoliday(Date holiday, Date d) {
        return d.getYear() == holiday.getYear() && //
                d.getMonth() == holiday.getMonth() && //
                d.getDate() == holiday.getDate();
    }

}
