package dojo.liftpasspricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PricesTest {


    private HolidaysProvider holidaysProvider;
    private CostForTypeProvider costForTypeProvider;
    private Prices prices;

    @BeforeEach
    void setUp() {
        holidaysProvider = new HolidaysProvider() {
            @Override
            public List<Date> getHolidays() throws SQLException {
                return null;
            }
        };
        costForTypeProvider = new CostForTypeProvider() {
            @Override
            public int getCostForLiftTicketType(String liftTicketType) throws SQLException {
                return 17;
            }

            @Override
            public void setLiftPassCostForLiftPassType(int liftPassCost, String liftPassType) throws SQLException {
            }
        };
        prices = new Prices();
    }

    @Test
    public void threeYearOldPeopleShouldBeFree() throws SQLException, ParseException {
        assertEquals("{ \"cost\": 0}",
                prices.getPricesHandler(costForTypeProvider,
                        holidaysProvider,
                        "3",
                        "night",
                        "2023-08-10"));
    }
}
