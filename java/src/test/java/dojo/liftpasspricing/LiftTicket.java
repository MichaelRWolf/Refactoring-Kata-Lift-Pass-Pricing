package dojo.liftpasspricing;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Integer.*;

public class LiftTicket {
    private final String liftTicketType;
    private final Date usageDate;
    private final Integer skierAge;

    @Override
    public String toString() {
        return "{"
                + liftTicketType + ", "
                + decoratedUsageDate() + ", "
                + skierAge
                + "}";
    }

    private String decoratedUsageDate() {
        SimpleDateFormat iso8601DateFormatter = new SimpleDateFormat("(EEE) yyyy-MM-dd");
        return iso8601DateFormatter.format(this.usageDate)
                + (isSpecialDay() ? "/special" : "")
                + (isHoliday() ? "/holiday" : "");
    }

    private boolean isSpecialDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(usageDate);
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

    private boolean isHoliday() {
        return isChristmas();
    }

    private boolean isChristmas() {
        Calendar usageDateCalendar = Calendar.getInstance();
        usageDateCalendar.setTime(usageDate);
        return usageDateCalendar.get(Calendar.MONTH) == Calendar.DECEMBER
                && usageDateCalendar.get(Calendar.DAY_OF_MONTH) == 25;
    }

    public LiftTicket(String liftTicketType, Date usageDate, String skierAge) {
        this.liftTicketType = liftTicketType;
        this.usageDate = usageDate;

        Integer ageMaybe;
        try {
            ageMaybe = parseInt(skierAge);
        } catch(NumberFormatException e) {
            ageMaybe = null;
        }
        this.skierAge = ageMaybe;
    }

    public String getLiftTicketType() {
        return liftTicketType;
    }

    public int getSkierAge() {
        return skierAge;
    }

    public Object getUsageDate() {
        return usageDate;
    }
}