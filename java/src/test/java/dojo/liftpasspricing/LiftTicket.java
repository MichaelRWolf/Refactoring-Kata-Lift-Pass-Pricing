package dojo.liftpasspricing;

import java.util.Date;

public class LiftTicket {
    private final String liftTicketType;
    private final Date usageDate;
    private final int skierAge;

    @Override
    public String toString() {
        return "{liftTicketType: " + liftTicketType + ", " +
                "usageDate: " + usageDate + ", " +
                "skierAge: " + skierAge + "}";
    }

    public LiftTicket(String liftTicketType, Date usageDate, int skierAge) {
        this.liftTicketType = liftTicketType;
        this.usageDate = usageDate;
        this.skierAge = skierAge;
    }
}
