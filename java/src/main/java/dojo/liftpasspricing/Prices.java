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
            int liftPassCostFromReq = Integer.parseInt(req.queryParams("cost"));
            String liftPassTypeFromReq = req.queryParams("type");

            dbu.setLiftPassCostForLiftPassType(liftPassCostFromReq, liftPassTypeFromReq);

            return "";
        });

        get("/prices", (req, res) -> {
            final Integer ageFromReq = req.queryParams("age") != null ? Integer.valueOf(req.queryParams("age")) : null;
            String liftPassTypeFromReq = req.queryParams("type");
            String dateFromReq = req.queryParams("date");

            // TODO: Refactor
            try (PreparedStatement costStmt = dbu.getConnection().prepareStatement( //
                    "SELECT cost FROM base_price " + //
                    "WHERE type = ?")) {
                costStmt.setString(1, liftPassTypeFromReq);
                try (ResultSet result = costStmt.executeQuery()) {
                    result.next();

                    int reduction;

                    if (ageFromReq != null && ageFromReq < 6) {
                        int costAfterAdjustments = 0;
                        return costAsJsonString(costAfterAdjustments);
                    } else {
                        reduction = 0;

                        int costFromResult = result.getInt("cost");
                        if (!liftPassTypeFromReq.equals("night")) {
                            DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

                            boolean isHoliday = isDateFromRequestAHoliday(dbu, dateFromReq, isoFormat);

                            if (dateFromReq != null) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(isoFormat.parse(dateFromReq));
                                if (!isHoliday && calendar.get(Calendar.DAY_OF_WEEK) == 2) {
                                    reduction = 35;
                                }
                            }

                            // TODO apply reduction for others
                            if (ageFromReq != null && ageFromReq < 15) {
                                int costAfterAdjustments = (int) Math.ceil(costFromResult * .7);
                                return costAsJsonString(costAfterAdjustments);
                            } else {
                                if (ageFromReq == null) {
                                    int costAfterAdjustments = (int) Math.ceil(costFromResult * (1 - reduction / 100.0));
                                    return costAsJsonString(costAfterAdjustments);
                                } else {
                                    if (ageFromReq > 64) {
                                        int costAfterAdjustments = (int) Math.ceil(costFromResult * .75 * (1 - reduction / 100.0));
                                        return costAsJsonString(costAfterAdjustments);
                                    } else {
                                        int costAfterAdjustments = (int) Math.ceil(costFromResult * (1 - reduction / 100.0));
                                        return costAsJsonString(costAfterAdjustments);
                                    }
                                }
                            }
                        } else {
                            if (ageFromReq != null && ageFromReq >= 6) {
                                if (ageFromReq > 64) {
                                    int costAfterAdjustments = (int) Math.ceil(costFromResult * .4);
                                    return costAsJsonString(costAfterAdjustments);
                                } else {
                                    int costAfterAdjustments = costFromResult;
                                    return costAsJsonString(costAfterAdjustments);
                                }
                            } else {
                                int costAfterAdjustments = 0;
                                return costAsJsonString(costAfterAdjustments);
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

    private String costAsJsonString(int costAfterAdjustments) {
        return "{ \"cost\": " + costAfterAdjustments + "}";
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







