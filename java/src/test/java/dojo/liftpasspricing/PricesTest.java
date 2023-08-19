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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PricesTest {
    // I like whitespace...



    private HolidaysProvider holidaysProvider;
    private CostForTypeProvider costForTypeProvider;
    private Prices prices;

    @BeforeEach
    void setUp() {
        holidaysProvider = () -> {
            List<Date> holidays = new ArrayList<>();
            Date holiday;
            try {
                holiday = new SimpleDateFormat("yyy-MM-dd").parse("2023-12-25");
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            holidays.add(holiday);
            return holidays;
        };
        costForTypeProvider = new CostForTypeProvider() {
            @Override
            public int getCostForLiftTicketType(String liftTicketType) {
                // INSERT INTO lift_pass.base_price (type, cost) VALUES ('1jour', 35);
                // INSERT INTO lift_pass.base_price (type, cost) VALUES ('night', 19);
                if (liftTicketType.equals("1jour")) {
                    return 35;
                } else if (liftTicketType.equals("night")) {
                    return 19;
                } else {
                    throw new RuntimeException("Unknown lift ticket type: " + liftTicketType);
                }
            }

            @Override
            public void setLiftPassCostForLiftPassType(int liftPassCost, String liftPassType) {
            }
        };
        prices = new Prices();
    }

    @Test
    public void allPermutationsOfDateAndTicketTypeAndAgeReturnCostAsJson() throws ParseException {
        StringBuilder result = new StringBuilder();
        Options verifyOptions = new Options().
                withReporter(new MultiReporter(
                        new MeldMergeReporter(),
                        new ClipboardReporter()
                ));

        ArrayList<String> usageDateStrings = new ArrayList<>();
        usageDateStrings.add("2023-08-15"); // Tue
        usageDateStrings.add("2023-12-25"); // Mon - /special/holiday
        usageDateStrings.add("2022-12-25"); // Sun - /holiday
        usageDateStrings.add("2023-08-14"); // Mon - /special


        List<String> liftTicketTypes = new ArrayList<>();
        liftTicketTypes.add("1jour");
        liftTicketTypes.add("night");


        List<String> agesAsString = IntStream.rangeClosed(1, 70)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());

        agesAsString.add(null);

        // TODO:
        //        banana.add("forty two");

        for (String usageDateString : usageDateStrings) {
            for (String liftTicketType : liftTicketTypes) {
                for (String age : agesAsString) {
                    LiftTicket ticket = getLiftTicket(usageDateString, liftTicketType, age);
                    String costAsJson = costAsJson(costForTypeProvider, holidaysProvider, ticket);
                    result.append(ticket).append(" => ").append(costAsJson).append("\n");
                }
            }
        }

        Approvals.verify(result.toString(), verifyOptions);
    }

    private String costAsJson(CostForTypeProvider costForTypeProvider,
                              HolidaysProvider holidaysProvider,
                              LiftTicket ticket) {

        String json;

        prices = new Prices();
        try {
            json = prices.getCostAsJson(costForTypeProvider, holidaysProvider, String.valueOf(ticket.getSkierAge()), ticket.getLiftTicketType(), new SimpleDateFormat("yyyy-MM-dd").format(ticket.getUsageDate()));
        } catch (Exception e) {
            json = "{ \"cost\": " + "NaN" + "}";
        }

        return json;
    }

    private LiftTicket getLiftTicket(String usageDateString, String liftTicketType, String skierAge) throws ParseException {
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
