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

            return new Prices().figTree(connection, req, age);
        });

        after((req, res) -> {
            res.type("application/json");
        });

        return connection;
    }

    private static int getCeil(double cost) {
        return (int) Math.ceil(cost);
    }

    private static boolean isNight(Request req) {
        return req.queryParams("type").equals("night");
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

    private static boolean areDatesEqual(Date holiday, Date d) {
        return d.getYear() == holiday.getYear() && //
                d.getMonth() == holiday.getMonth() && //
                d.getDate() == holiday.getDate();
    }

    private String businessLogicWithConnection_and_stuff(Request req,
                                                         Integer age,
                                                         DbArtifactCostByType dbArtifactCostByType)
            throws SQLException, ParseException {
        boolean isHoliday = false;

        int reduction = 0;
        int costBase = dbArtifactCostByType.getCost();
        int elderberryCost;
        int elderberryCost2;

        if (age == null) {
            if (isNight(req)) {
                elderberryCost = 0;
                return stringyObjectWithCostMember(elderberryCost);
            } else {
                DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

                String formDateAsIsoFormat = req.queryParams("date");
                isHoliday = isHoliday(dbArtifactCostByType, isHoliday, isoFormat, formDateAsIsoFormat);

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
        isHoliday = isHoliday(dbArtifactCostByType, isHoliday, isoFormat, formDateAsIsoFormat);

        if (formDateAsIsoFormat != null) {
            if (isNonHolidayAndIsLowerCostDay(isHoliday, isoFormat, formDateAsIsoFormat)) {
                reduction = 35;
            }
        }
        return banana_fn(age, dbArtifactCostByType, reduction);
    }

    private String banana_fn(Integer age, DbArtifactCostByType dbArtifactCostByType, int reduction) throws SQLException {
        int costFromResultSet = dbArtifactCostByType.getCost();
        double ageFactor = 1;

        if (age != null) {
            if (age < 15) {
                ageFactor = .7;
                reduction = 0;
            } else if (age > 64) {
                ageFactor = .75;
            }
        }

        int bananaCost = getCeil(costFromResultSet * ageFactor * reductionAsIntToFactorAsFloat(reduction));
        return stringyObjectWithCostMember(bananaCost);
    }

    private boolean isHoliday(DbArtifactCostByType dbArtifactCostByType,
                              boolean isHoliday,
                              DateFormat isoFormat,
                              String formDateAsIsoFormat)
            throws SQLException, ParseException {
        boolean isHoliday1 = isHoliday;
        Connection connection = dbArtifactCostByType.getConnection();
        try (PreparedStatement holidayStmt = connection.prepareStatement(SELECT_ALL_FROM_HOLIDAYS)) {
            try (ResultSet holidaysResultSet = holidayStmt.executeQuery()) {
                // TODO: Separate getting holidaysListOfDate (biz sense - Date) from holidaysResultSet (DB sense - ResultSet)
                // Accumulate holidays into a list
                // Then pass that list to the loop that
                // TODO: Find provable refactoring to separate parts of a body of a while loop

/* Option 1: Get single holiday as part of while control.  Use the single date in the body
                while (Date holiday = holidaysResultSet.next().getDate("holiday") {
                    // do isHoliday
                }
*/
                // Option 2:  Collect holidayAsDate in one loop
                // Then use the list in the body of the while loop
                while (holidaysResultSet.next()) {
                    Date holiday = holidaysResultSet.getDate("holiday");
                    isHoliday1 = isHolidayAccountingForUnFormatableDates(
                            isoFormat,
                            formDateAsIsoFormat,
                            isHoliday1,
                            holiday);
                }
                isHoliday = isHoliday1;
            }
        }
        return isHoliday;
    }

    private boolean isHolidayAccountingForUnFormatableDates(DateFormat isoFormat, String formDateAsIsoFormat, boolean isHoliday1, Date holiday) throws ParseException {
        if (formDateAsIsoFormat != null) {
            Date form_date = isoFormat.parse(formDateAsIsoFormat);
            if (areDatesEqual(holiday, form_date)) {
                isHoliday1 = true;
            }
        }
        return isHoliday1;
    }

    // TODO: Move to DB artifact
    private String DbSelectCostByType_eventually_inlined(DatabaseArtifact_maybe_CRUD databaseArtifact_maybe_CRUD,
                                                         Request req,
                                                         Integer age)
            throws SQLException, ParseException {
        Connection connection = databaseArtifact_maybe_CRUD.getConnection();
        PreparedStatement costStmt = databaseArtifact_maybe_CRUD.getCostStmt();
        try (ResultSet result = costStmt.executeQuery()) {
            result.next();

            return businessLogicWithConnection_and_stuff(req, age, new DbArtifactCostByType(connection, result));

            // TODO apply reduction for others
        }
    }

    // TODO: Move to DB artifact
    private String figTree(Connection connection, Request req, Integer age)
            throws SQLException, ParseException {
        try (PreparedStatement costStmt = connection.prepareStatement(SQL_SELECT_cost)) {
            costStmt.setString(1, req.queryParams("type"));
            return DbSelectCostByType_eventually_inlined(new DatabaseArtifact_maybe_CRUD(connection, costStmt), req, age);
        }
    }
}
