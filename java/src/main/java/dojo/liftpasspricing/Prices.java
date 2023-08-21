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
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

        double cost;
        int reduction;
        if (liftTicketTypeString.equals("night")) {
            reduction = getNightReduction(age);
            cost = calculateCostReduction(costForLiftTicketTypeFromDatabase, reduction);
        } else {
            reduction = getNonNightReduction(holidaysProvider, dateString, age, isoFormat);
            cost = calculateCostReduction(costForLiftTicketTypeFromDatabase, reduction);

            // Seniors get an additional reduction
            if (age != null && age > 64) {
                int reduction_additional_senior = 25;
                cost *= reductionOff_1_to_100__to__factorOn_0_to_1(reduction_additional_senior);
            }
        }
        return getJsonForCost(cost);
    }

    private int getNonNightReduction(HolidaysProvider holidaysProvider, String dateString, Integer age, SimpleDateFormat isoFormat) throws ParseException, SQLException {
        int reduction = 0;
        if (age == null) {
            if (isSpecialDayButNotHoliday(holidaysProvider, dateString, isoFormat)) {
                reduction = 35;
            }
        } else {
            if (age < 6) {
                reduction = 100;
            } else {
                if (isSpecialDayButNotHoliday(holidaysProvider, dateString, isoFormat)) {
                    reduction = 35;
                }
                if (age < 15) {
                    reduction = 30;
                }
            }
        }
        return reduction;
    }

    private boolean isSpecialDayButNotHoliday(HolidaysProvider holidaysProvider, String dateString, SimpleDateFormat isoFormat) throws ParseException, SQLException {
        return (dateString != null) && isSpecialDay(dateString, isoFormat) && isNotHoliday(holidaysProvider, dateString, isoFormat);
    }

    private int getNightReduction(Integer age) {
        if (age == null) {
            return 100;
        } else if (age < 6) {
            return 100;
        } else if (64 < age) {
            return 60;
        } else {
            return 0;
        }
    }

    private double calculateCostReduction(int costForLiftTicketTypeFromDatabase, int reduction) {
        return costForLiftTicketTypeFromDatabase * reductionOff_1_to_100__to__factorOn_0_to_1(reduction);
    }

    private boolean isNotHoliday(HolidaysProvider holidaysProvider, String dateString, DateFormat isoFormat) throws SQLException, ParseException {
        return !isDateFromRequestAHoliday(holidaysProvider, dateString, isoFormat);
    }

    private boolean isSpecialDay(String dateString, DateFormat isoFormat) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(isoFormat.parse(dateString));
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

    private String getJsonForCost(double cost) {
        return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
    }

    private double reductionOff_1_to_100__to__factorOn_0_to_1(int reduction) {
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







