package dojo.liftpasspricing;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface DatabaseUtilities {
    List<Date> getHolidays() throws SQLException;
}
