package dojo.liftpasspricing;

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
                "usageDate: " + decorated(usageDate) +
                ", " +
                "skierAge: " + skierAge + "}";
    }

    private String decorated(Date usageDate) {
        return this.usageDate.toString()
                + (isHoliday() ?  "/holiday" : "");
    }

    private boolean isHoliday() {
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
