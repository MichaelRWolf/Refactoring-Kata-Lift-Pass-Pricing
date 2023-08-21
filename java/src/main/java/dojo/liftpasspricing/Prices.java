package dojo.liftpasspricing;

import spark.Request;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static spark.Spark.*;

public class Prices {

    public void createApplication(CostForTypeProvider costForTypeProvider, HolidaysProvider holidaysProvider) {
        port(4567);

        put("/prices", (req, res) -> putPricesHandler(costForTypeProvider, req));

        get("/prices", (req, res) -> getPricesHandler(costForTypeProvider, holidaysProvider, req.queryParams("age"), req.queryParams("type"), req.queryParams("date")));

        after((req, res) -> res.type("application/json"));
    }

    private String putPricesHandler(CostForTypeProvider costForTypeProvider, Request req) throws SQLException {
        int liftPassCost = Integer.parseInt(req.queryParams("cost"));
        String liftPassType = req.queryParams("type");

        costForTypeProvider.setLiftPassCostForLiftPassType(liftPassCost, liftPassType);

        return "";
    }

    String getPricesHandler(CostForTypeProvider costForTypeProvider,
                            HolidaysProvider holidaysProvider,
                            String ageString,
                            String liftTicketTypeString,
                            String dateString)
            throws
            SQLException,
            ParseException {
        return getCostAsJson(costForTypeProvider, holidaysProvider, ageString, liftTicketTypeString, dateString);
    }

    public String getCostAsJson(CostForTypeProvider costForTypeProvider,
                                HolidaysProvider holidaysProvider,
                                String ageString,
                                String liftTicketTypeString,
                                String dateString)
            throws SQLException, ParseException {
        final Integer age = ageString != null ? Integer.valueOf(ageString) : null;
        int costForLiftTicketTypeFromDatabase;

        costForLiftTicketTypeFromDatabase = costForTypeProvider.getCostForLiftTicketType(liftTicketTypeString);
        int reduction;
        double cost;

        if (age != null && age < 6) {
            cost = 0;
        } else {
            reduction = 0;

            if (liftTicketTypeString.equals("night")) {
                if (age == null) {
                    cost = 0;
                } else {
                    if (age > 64) {
                        reduction = 60;
                        cost = costForLiftTicketTypeFromDatabase * reductionOff_1to100_to_factorOn(reduction);
                    } else {
                        cost = costForLiftTicketTypeFromDatabase * reductionOff_1to100_to_factorOn(reduction);
                    }
                }
            } else {
                DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

                boolean isHoliday = isDateFromRequestAHoliday(holidaysProvider, dateString, isoFormat);

                if (dateString != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(isoFormat.parse(dateString));
                    if (!isHoliday && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                        reduction = 35;
                    }
                }
                // TODO apply reduction for others
                if (age != null && age < 15) {
                    reduction = 30;
                    cost = costForLiftTicketTypeFromDatabase * reductionOff_1to100_to_factorOn(reduction);
                } else {
                    if (age == null) {
                        cost = costForLiftTicketTypeFromDatabase * reductionOff_1to100_to_factorOn(reduction);
                    } else {
                        if (age > 64) {
                            int reduction2 = 25;
                            cost = costForLiftTicketTypeFromDatabase * reductionOff_1to100_to_factorOn(reduction) * reductionOff_1to100_to_factorOn(reduction2);
                        } else {
                            cost = costForLiftTicketTypeFromDatabase * reductionOff_1to100_to_factorOn(reduction);
                        }
                    }
                }
            }
        }
        return getJsonForCost(cost);
    }

    private String getJsonForCost(double cost) {
        return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
    }

    private double reductionOff_1to100_to_factorOn(int reduction) {
        return (100 - reduction) / 100.0;
    }

    private boolean isDateFromRequestAHoliday(HolidaysProvider holidaysProvider, String dateFromRequest, DateFormat isoFormat) throws SQLException, ParseException {
        List<Date> holidays = holidaysProvider.getHolidays();
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

    @SuppressWarnings("deprecation")
    private boolean areDatesEqual(Date holiday, Date d) {
        return d.getYear() == holiday.getYear() && //
                d.getMonth() == holiday.getMonth() && //
                d.getDate() == holiday.getDate();
    }

}

// dbu.lookForDate(isRightDate







