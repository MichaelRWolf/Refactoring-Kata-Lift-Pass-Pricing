package dojo.liftpasspricing;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LiftTicket {
    private final String liftTicketType;
    private final Date usageDate;
    private final int skierAge;

    @Override
    public String toString() {
        return "{liftTicketType: " + liftTicketType + ", " +
                // Decorate date with /holiday if it's a holiday
                "usageDate: " + decoratedUsageDate() +
                ", " +
                "skierAge: " + skierAge + "}";
    }

    private String decoratedUsageDate() {
        SimpleDateFormat iso8601DateFormater = new SimpleDateFormat("(EEE) yyyy-MM-dd");
        return iso8601DateFormater.format(this.usageDate)
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

    public LiftTicket(String liftTicketType, Date usageDate, int skierAge) {
        this.liftTicketType = liftTicketType;
        this.usageDate = usageDate;
        this.skierAge = skierAge;
    }

    public String getLiftTicketType() {
        return liftTicketType;
    }
}
