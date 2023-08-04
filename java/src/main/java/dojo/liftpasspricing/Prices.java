package dojo.liftpasspricing;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static spark.Spark.*;

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

                        int costForLiftTicketTypeFromDatabase = result.getInt("cost");
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
                                return "{ \"cost\": " + (int) Math.ceil(costForLiftTicketTypeFromDatabase * .7) + "}";
                            } else {
                                if (age == null) {
                                    double cost = costForLiftTicketTypeFromDatabase * (1 - reduction / 100.0);
                                    return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                                } else {
                                    if (age > 64) {
                                        double cost = costForLiftTicketTypeFromDatabase * .75 * (1 - reduction / 100.0);
                                        return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                                    } else {
                                        double cost = costForLiftTicketTypeFromDatabase * (1 - reduction / 100.0);
                                        return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                                    }
                                }
                            }
                        } else {
                            if (age != null && age >= 6) {
                                if (age > 64) {
                                    return "{ \"cost\": " + (int) Math.ceil(costForLiftTicketTypeFromDatabase * .4) + "}";
                                } else {
                                    return "{ \"cost\": " + costForLiftTicketTypeFromDatabase + "}";
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
        List<Date> holidays = dbu.getHolidays();
        // Business logic
        boolean isHoliday = false;
        for (Date holiday : holidays) {
            if (dateFromRequest != null) { //
                Date d = isoFormat.parse(dateFromRequest);
                if (areDatesEqual(holiday, d)) {
                    isHoliday = true;
                }
            }
        }
        return isHoliday;
    }

    private boolean areDatesEqual(Date holiday, Date d) {
        return d.getYear() == holiday.getYear() && //
                d.getMonth() == holiday.getMonth() && //
                d.getDate() == holiday.getDate();
    }

}

// dbu.lookForDate(isRightDate







