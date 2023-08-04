package dojo.liftpasspricing;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface HolidaysProvider {
    List<Date> getHolidays() throws SQLException;
}
