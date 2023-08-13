package dojo.liftpasspricing;

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.reporters.ClipboardReporter;
import org.approvaltests.reporters.MultiReporter;
import org.approvaltests.reporters.linux.MeldMergeReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PricesTest {


    private HolidaysProvider holidaysProvider;
    private CostForTypeProvider costForTypeProvider;
    private Prices prices;

    @BeforeEach
    void setUp() {
        holidaysProvider = () -> null;
        costForTypeProvider = new CostForTypeProvider() {
            @Override
            public int getCostForLiftTicketType(String liftTicketType) {
                return 17;
            }

            @Override
            public void setLiftPassCostForLiftPassType(int liftPassCost, String liftPassType) {
            }
        };
        prices = new Prices();
    }

    @Test
    public void testApprovalTestFrameworkWorks() throws ParseException {
        String result;
        Options verifyOptions = new Options().
                withReporter(new MultiReporter(
                        new MeldMergeReporter(),
                        new ClipboardReporter()
                ));

        Date usageDate = new SimpleDateFormat("yyyy-MM-dd").parse("2023-08-15");
        int skierAge = 25;
        LiftTicket ticket = new LiftTicket("night", usageDate, skierAge);
        result = ticket.toString();
        Approvals.verify(result, verifyOptions);
    }


    @Test
    public void threeYearOldPeopleShouldBeFree() throws Exception {
        assertEquals("{ \"cost\": 0}",
                prices.getPricesHandler(costForTypeProvider,
                        holidaysProvider,
                        "3",
                        "night",
                        "2023-08-10"));
    }
}
