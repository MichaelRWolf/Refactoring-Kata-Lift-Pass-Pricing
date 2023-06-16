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

    public static final String SQL_INSERT_type_cost =
            "INSERT INTO base_price (type, cost) VALUES (?, ?) " + //
                    "ON DUPLICATE KEY UPDATE cost = ?";
    public static final String SQL_SELECT_cost =
            "SELECT cost FROM base_price " + //
                    "WHERE type = ?";
    public static final String SELECT_ALL_FROM_HOLIDAYS =
            "SELECT * FROM holidays";

    public static Connection createApp() throws SQLException {

        final Connection connection =
                DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/lift_pass",
                        "root",
                        "mysql"
                );

        port(4567);

        put("/prices", (req, res) -> {
            int liftPassCost = Integer.parseInt(req.queryParams("cost"));
            String liftPassType = req.queryParams("type");

            try (PreparedStatement stmt = connection.prepareStatement(SQL_INSERT_type_cost)) {
                stmt.setString(1, liftPassType);
                stmt.setInt(2, liftPassCost);
                stmt.setInt(3, liftPassCost);
                stmt.execute();
            }

            return "";
        });

        get("/prices", (req, res) -> {
            final Integer age = req.queryParams("age") != null ? Integer.valueOf(req.queryParams("age")) : null;

            try (PreparedStatement costStmt = connection.prepareStatement(SQL_SELECT_cost)) {
                costStmt.setString(1, req.queryParams("type"));
                return cherry_temp(new DatabaseArtifact_maybe_CRUD(connection, costStmt), req, age);
            }
        });

        after((req, res) -> {
            res.type("application/json");
        });

        return connection;
    }

    private static String cherry_temp(DatabaseArtifact_maybe_CRUD databaseArtifact_maybe_CRUD,
                                      Request req,
                                      Integer age)
            throws SQLException, ParseException {
        Connection connection = databaseArtifact_maybe_CRUD.getConnection();
        PreparedStatement costStmt = databaseArtifact_maybe_CRUD.getCostStmt();
        try (ResultSet result = costStmt.executeQuery()) {
            result.next();

            boolean isHoliday = false;

            int reduction = 0;
            int costBase = getCost(result);
            int elderberryCost;
            int elderberryCost2;

            if (age == null) {
                if (isNight(req)) {
                    elderberryCost = 0;
                    return stringyObjectWithCostMember(elderberryCost);
                } else {
                    DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

                    String formDateAsIsoFormat = req.queryParams("date");
                    isHoliday = isHolidayFromConnection_and_other_params(
                            connection,
                            isHoliday,
                            isoFormat,
                            formDateAsIsoFormat
                    );

                    if (formDateAsIsoFormat != null) {
                        if (isNonHolidayAndIsLowerCostDay(isHoliday, isoFormat, formDateAsIsoFormat)) {
                            reduction = 35;
                        }
                    }

                    double cost;
                    cost = costBase * reductionAsIntToFactorAsFloat(reduction);
                    elderberryCost = getCeil(cost);
                    return stringyObjectWithCostMember(elderberryCost);
                }
            } else if (age < 6) {
                elderberryCost = 0;
                return stringyObjectWithCostMember(elderberryCost);
            } else if (age > 64) {
                if (isNight(req)) {
                    double magicNumber = .4;
                    double elderNightCost = costBase * magicNumber;
                    elderberryCost = getCeil(elderNightCost);
                    return stringyObjectWithCostMember(elderberryCost);
                }
            } else {
                if (isNight(req)) {
                    elderberryCost = costBase;
                    return stringyObjectWithCostMember(elderberryCost);
                }
            }

            DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

            String formDateAsIsoFormat = req.queryParams("date");
            isHoliday = isHolidayFromConnection_and_other_params(connection,
                    isHoliday,
                    isoFormat,
                    formDateAsIsoFormat);

            if (formDateAsIsoFormat != null) {
                if (isNonHolidayAndIsLowerCostDay(isHoliday, isoFormat, formDateAsIsoFormat)) {
                    reduction = 35;
                }
            }
            return banana_fn(age, result, reduction);

            // TODO apply reduction for others
        }
    }

    // Currentl6y pasing 2 DB params.... connection and preparedstatement
    // Want.... 1 DB artifact, probably Dataase Repositor

    private static int getCeil(double cost) {
        return (int) Math.ceil(cost);
    }

    private static boolean isHolidayFromConnection_and_other_params(Connection connection,
                                                                    boolean isHoliday,
                                                                    DateFormat isoFormat,
                                                                    String formDateAsIsoFormat)
            throws SQLException, ParseException {
        try (PreparedStatement holidayStmt = connection.prepareStatement(SELECT_ALL_FROM_HOLIDAYS)) {
            try (ResultSet holidays = holidayStmt.executeQuery()) {
                isHoliday = isAnyHoliday(isHoliday, isoFormat, formDateAsIsoFormat, holidays);
            }
        }
        return isHoliday;
    }

    private static boolean isNight(Request req) {
        return req.queryParams("type").equals("night");
    }

    private static int getCost(ResultSet result) throws SQLException {
        return result.getInt("cost");
    }

    private static String banana_fn(Integer age, ResultSet result, int reductionPercentageAsInt) throws SQLException {
        int bananaCost;
        int costFromResultSet = getCost(result);
        double ageFactor = 1;

        if (age != null) {
            if (age < 15) {
                ageFactor = .7;
                reductionPercentageAsInt = 0;
            } else if (age > 64) {
                ageFactor = .75;
            }
        }

        bananaCost = getCeil(costFromResultSet * ageFactor * reductionAsIntToFactorAsFloat(reductionPercentageAsInt));
        return stringyObjectWithCostMember(bananaCost);
    }

    private static String stringyObjectWithCostMember(int bananaCost) {
        return "{ \"cost\": " + bananaCost + "}";
    }

    private static double reductionAsIntToFactorAsFloat(int reductionPercentageAsInt) {
        return 1 - reductionPercentageAsInt / 100.0;
    }

    private static boolean isNonHolidayAndIsLowerCostDay(boolean isHoliday, DateFormat isoFormat, String
            formDateAsIsoFormat) throws ParseException {
        return !isHoliday && isLowerCostDay(isoFormat, formDateAsIsoFormat);
    }

    private static boolean isLowerCostDay(DateFormat isoFormat, String formDateAsIsoFormat) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(isoFormat.parse(formDateAsIsoFormat));
        return calendar.get(Calendar.DAY_OF_WEEK) == 2;
    }

    private static boolean isAnyHoliday(boolean isHoliday, DateFormat isoFormat, String
            formDateAsIsoFormat, ResultSet holidayResultSet) throws SQLException, ParseException {
        while (holidayResultSet.next()) {
            Date holiday = holidayResultSet.getDate("holiday");
            if (formDateAsIsoFormat != null) {
                Date form_date = isoFormat.parse(formDateAsIsoFormat);
                if (areDatesEqual(holiday, form_date)) {
                    isHoliday = true;
                }
            }
        }
        return isHoliday;
    }

    private static boolean areDatesEqual(Date holiday, Date d) {
        return d.getYear() == holiday.getYear() && //
                d.getMonth() == holiday.getMonth() && //
                d.getDate() == holiday.getDate();
    }

}
