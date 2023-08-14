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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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


        List<Integer> ages = IntStream.rangeClosed(1, 70)
                .boxed()
                .collect(Collectors.toList());


        for (String usageDateString : usageDateStrings) {
            for (String liftTicketType : liftTicketTypes) {
                for (int age : ages) {
                    LiftTicket ticket = getLiftTicket(usageDateString, liftTicketType, age);
                    String costAsJson = costAsJson(costForTypeProvider, holidaysProvider, ticket);
                    result.append(ticket).append(" => ").append(costAsJson).append("\n");
                }
            }
        }


        Approvals.verify(result.toString(), verifyOptions);
    }

    private String costAsJson(CostForTypeProvider costForTypeProvider, HolidaysProvider holidaysProvider, LiftTicket ticket) {
        return "{ \"cost\": 17}";
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
