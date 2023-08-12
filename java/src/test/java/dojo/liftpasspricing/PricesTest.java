package dojo.liftpasspricing;

import org.approvaltests.core.Options;
import org.approvaltests.reporters.ClipboardReporter;
import org.approvaltests.reporters.MultiReporter;
import org.approvaltests.reporters.linux.MeldMergeReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;


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
    public void testApprovalTestFrameworkWorks() throws SQLException, ParseException {
        String result = "FortyTwo";
     /*
        // Provide input parameters for the getPricesHandler method
        String age = "25";  // Age as a string
        String liftTicketType = "regular";
        String dateString = "2023-08-15"; // A sample date

        // Call the method and get the result
        result = prices.getPricesHandler(costForTypeProvider, holidaysProvider, age, liftTicketType, dateString);
*/
        // TODO: Move this file from /java to /java-simple (where we did other work)
        Approvals.verify(result,
                new Options().
                        withReporter(new MultiReporter(
                                new MeldMergeReporter(),
                                new ClipboardReporter()
                        )));
//        Approvals.verify(result, new Options()
//                .withReporter(new MeldMergeReporter())
//                .withReporter(new ClipboardReporter())
//        );
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
