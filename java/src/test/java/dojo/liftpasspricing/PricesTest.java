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
import java.util.ArrayList;
import java.util.List;

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
        StringBuilder result = new StringBuilder();
        Options verifyOptions = new Options().
                withReporter(new MultiReporter(
                        new MeldMergeReporter(),
                        new ClipboardReporter()
                ));

        ArrayList<String> usageDateStrings = new ArrayList<>();
        usageDateStrings.add("2023-08-15");
        usageDateStrings.add("2023-12-25");


        List<String> liftTicketTypes = new ArrayList<>();
        liftTicketTypes.add("regular");
        liftTicketTypes.add("night");
        // TODO:
        // liftTicketTypes.add(null);

        for (String usageDateString1 : usageDateStrings) {
            for (String liftTicketType : liftTicketTypes) {
                for (int age = 1; age <= 70; age++) {
                    LiftTicket ticket = getLiftTicket(usageDateString1, liftTicketType, age);
                    result.append(ticket).append("\n");
                }
            }
        }


        Approvals.verify(result.toString(), verifyOptions);
    }

    private LiftTicket getLiftTicket(String usageDateString, String liftTicketType, int skierAge) throws ParseException {
        return new LiftTicket(liftTicketType, new SimpleDateFormat("yyyy-MM-dd").parse(usageDateString), skierAge);
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
