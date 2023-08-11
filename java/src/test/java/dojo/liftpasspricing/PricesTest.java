package dojo.liftpasspricing;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PricesTest {

    @BeforeAll
    public static void createPrices() throws SQLException {
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
    }

    @Test
    public void doesSomething() {
        assertEquals(2+2, 4);
    }

    @Test
    public void doPutGetHandlerBusinessLogic() {
        // write some tests for the business logic
    }

}
