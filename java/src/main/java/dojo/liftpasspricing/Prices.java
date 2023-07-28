package dojo.liftpasspricing;

import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.put;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Prices {

    public DatabaseUtilities createApp() {
        DatabaseUtilities dbu = new DatabaseUtilities();

        port(4567);

        put("/prices", (req, res) -> {
            int liftPassCost = Integer.parseInt(req.queryParams("cost"));
            String liftPassType = req.queryParams("type");

            dbu.setLiftPassCostForLiftPassType(liftPassCost, liftPassType);

            return "";
        });

        get("/prices", (req, res) -> {
            final Integer age = req.queryParams("age") != null ? Integer.valueOf(req.queryParams("age")) : null;

            try (PreparedStatement costStmt = dbu.getConnection().prepareStatement( //
                    "SELECT cost FROM base_price " + //
                    "WHERE type = ?")) {
                costStmt.setString(1, req.queryParams("type"));
                try (ResultSet result = costStmt.executeQuery()) {
                    result.next();

                    int reduction;

                    if (age != null && age < 6) {
                        return "{ \"cost\": 0}";
                    } else {
                        reduction = 0;

                        if (!req.queryParams("type").equals("night")) {
                            String dateFromRequest = req.queryParams("date");
                            DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

                            boolean isHoliday = isDateFromRequestAHoliday(dbu, dateFromRequest, isoFormat);

                            if (dateFromRequest != null) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(isoFormat.parse(dateFromRequest));
                                if (!isHoliday && calendar.get(Calendar.DAY_OF_WEEK) == 2) {
                                    reduction = 35;
                                }
                            }

                            // TODO apply reduction for others
                            if (age != null && age < 15) {
                                return "{ \"cost\": " + (int) Math.ceil(result.getInt("cost") * .7) + "}";
                            } else {
                                if (age == null) {
                                    double cost = result.getInt("cost") * (1 - reduction / 100.0);
                                    return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                                } else {
                                    if (age > 64) {
                                        double cost = result.getInt("cost") * .75 * (1 - reduction / 100.0);
                                        return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                                    } else {
                                        double cost = result.getInt("cost") * (1 - reduction / 100.0);
                                        return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                                    }
                                }
                            }
                        } else {
                            if (age != null && age >= 6) {
                                if (age > 64) {
                                    return "{ \"cost\": " + (int) Math.ceil(result.getInt("cost") * .4) + "}";
                                } else {
                                    return "{ \"cost\": " + result.getInt("cost") + "}";
                                }
                            } else {
                                return "{ \"cost\": 0}";
                            }
                        }
                    }
                }
            }
        });

        after((req, res) -> {
            res.type("application/json");
        });
        return dbu;
    }

    private boolean isDateFromRequestAHoliday(DatabaseUtilities dbu, String dateFromRequest, DateFormat isoFormat) throws SQLException, ParseException {
        try (PreparedStatement holidayStmt = dbu.getConnection().prepareStatement( // #2 - 110 -- DB stuff - isolate for testing
                "SELECT * FROM holidays")) {
            try (ResultSet holidaysResultSet = holidayStmt.executeQuery()) {

                // #1 - init
                boolean isHoliday = false;
                while (holidaysResultSet.next()) {
                    if (dateFromRequest != null) { //
                        Date d = isoFormat.parse(dateFromRequest);
                        if (areDatesEqual(holiday, d)) {
                            isHoliday = true;
                        }
                    }
                }
                return isHoliday;
            }
        }
    }

    private boolean areDatesEqual(Date holiday, Date d) {
        return d.getYear() == holiday.getYear() && //
                d.getMonth() == holiday.getMonth() && //
                d.getDate() == holiday.getDate();
    }

}

// dbu.lookForDate(isRightDate







