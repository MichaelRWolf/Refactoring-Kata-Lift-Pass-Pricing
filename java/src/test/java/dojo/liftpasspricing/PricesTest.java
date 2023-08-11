package dojo.liftpasspricing;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PricesTest {


    @Test
    public void testGetPricesHandler() throws SQLException, ParseException {
        // given
        CostForTypeProvider costForTypeProvider = new CostForTypeProvider() {
            @Override
            public int getCostForLiftTicketType(String liftTicketType) throws SQLException {
                return 17;
            }

            @Override
            public void setLiftPassCostForLiftPassType(int liftPassCost, String liftPassType) throws SQLException {
            }
        };
        HolidaysProvider holidaysProvider = new HolidaysProvider() {
            @Override
            public List<Date> getHolidays() throws SQLException {
                return null;
            }
        };
        Prices prices = new Prices();
        assertEquals("{ \"cost\": 17}",
                prices.getPricesHandler(costForTypeProvider,
                        holidaysProvider,
                        "42",
                        "night",
                        "2023-08-10"));

        assertEquals("{ \"cost\": 0}",
                prices.getPricesHandler(costForTypeProvider,
                        holidaysProvider,
                        "3",
                        "night",
                        "2023-08-10"));
    }
}
