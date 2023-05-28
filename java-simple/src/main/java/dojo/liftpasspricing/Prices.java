package dojo.liftpasspricing;

import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.put;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static spark.Spark.*;

/*
 * TODO
 *
 * factor     ::== [0 ..   1]
 * percentage ::== [0 .. 100]
 *
 * percentage = 100 * factor
 * factor = percentage / 100
 *
 * priceAdjusted = priceBase + priceAdjustment        # priceBase.adjustByAmount(priceAdjustment)
 * priceAdjusted = priceBase - priceDecrease          # priceBase.reduceByAmount(priceDecrease)
 * priceAdjusted = priceBase + priceIncrease          # priceBase.increaseByAmount(priceIncrease)
 *
 *
 * priceFactor       = priceAdjusted       / priceBase  * ??? priceBase.priceFactor(priceAdjusted) ???
 * priceFactorForAge = priceAdjustedForAge / priceBase
 * 0.75              =                 150 / 200
 *
 * priceAdjusted       = priceBase * priceFactor
 * priceAdjustedForAge = priceBase * priceFactorForAge  # priceBase.adjustByFactor(priceFactorForAge)
 * 150                 =       200 * 0.75               # priceBase.adjustByFactor(0.75)
 *
 * priceAdjustmentPercent = priceFactor * 100 - 100
 * -25                     =       0.75 * 100 - 100
 *
 * priceAdjustmentPercent = 100 * (priceFactor - 1)
 * -25                    = 100 * (       0.75 - 1)
 *
 * priceAdjustmentPercent = 100 *  (priceAdjusted / priceBase - 1)
 * -25                    = 100 *  (          150 / 200       - 1)
 *
 * priceAdjustmentPercent = 100 * priceAdjected / priceBase - 100
 * -25                    = 100 *           150 / 200       - 100
 *
 *
 * priceAdjusted = priceBase * (priceAdjustmentPercent + 100) / 100
 * 150                   200 * (                   -25 + 100) / 100   # basePrice.adjustByPercent(-25)
 *                                                                    # basePrice.decreaseByPercent(25)
 *
 *
 * priceIncreasePercent = - priceAdjustmentPercent
 *                   25 = - -25
 *
 * priceDecreasePercent = + priceAdjustmentPercent
 *                  -25 = + -25
 *
 */

/*
 * 0.  Continually... `rename variable` to nudge in direction of naming conventions
 *
 * Database
 *   can make connection
 *   can query `lift_pass` table
 *   can query `holidays` table
 *
 * http connection
 *   can get some response from command-line tool
 *   can get some response from test/* file
 *   can fail->pass from test/* file
 *   can use preferred test framework -- Acceptance Tests, Given/When/Then (Cucumber?)
 */

/*
 * With running DB, start creating a seam (interface)
 *   can get SQL data from existing queries
 *   can get list of TicketTypes
 *   can get costBase for each TicketType
 *   can get list of ageMax (min?) for each age bracket
 *   can get priceDiscountPercent for each representative age
 *   can calculate priceCalculated as fn of TicketType and age
 *   can get priceDiscountPercent for a holiday (and non-holiday)
 *   can calculate priceCalculated as fn of TicketType and age
 * */
public class Prices {

    public static Connection createApp() throws SQLException {

        final Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lift_pass", "root", "mysql");

        port(4567);

        put("/prices", (req, res) -> {
            int liftPassCost = Integer.parseInt(req.queryParams("cost"));
            String liftPassType = req.queryParams("type");

            try (PreparedStatement stmt = connection.prepareStatement( //
                    "INSERT INTO base_price (type, cost) VALUES (?, ?) " + //
                            "ON DUPLICATE KEY UPDATE cost = ?")) {
                stmt.setString(1, liftPassType);
                stmt.setInt(2, liftPassCost);
                stmt.setInt(3, liftPassCost);
                stmt.execute();
            }

            return "";
        });

        get("/prices", (req, res) -> {
            final Integer age = req.queryParams("age") != null ? Integer.valueOf(req.queryParams("age")) : null;

            int basePrice;
            try (PreparedStatement costStmt = connection.prepareStatement( //
                    "SELECT cost FROM base_price " + //
                            "WHERE type = ?")) {
                costStmt.setString(1, req.queryParams("type"));
                try (ResultSet result = costStmt.executeQuery()) {
                    result.next();
                    basePrice = result.getInt("cost");
                }
            }

            if (age != null && age < 6) {
                return "{ \"cost\": 0}";
            } else {

                if (!req.queryParams("type").equals("night")) {
                    DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

                    List<Date> holidaysDates = new ArrayList<>();
                    try (PreparedStatement holidayStmt = connection.prepareStatement( //
                            "SELECT * FROM holidays")) {
                        try (ResultSet holidays = holidayStmt.executeQuery()) {

                            while (holidays.next()) {
                                holidaysDates.add(holidays.getDate("holiday"));
                            }
                        }
                    }

                    int priceReductionPercent = 0;
                    boolean isHoliday = false;
                    for (Date holiday : holidaysDates) {
                        if (req.queryParams("date") != null) {
                            Date d = isoFormat.parse(req.queryParams("date"));
                            if (d.getYear() == holiday.getYear() && //
                                    d.getMonth() == holiday.getMonth() && //
                                    d.getDate() == holiday.getDate()) {
                                isHoliday = true;
                            }
                        }
                    }

                    if (req.queryParams("date") != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(isoFormat.parse(req.queryParams("date")));
                        if (!isHoliday && calendar.get(Calendar.DAY_OF_WEEK) == 2) {
                            priceReductionPercent = 35;
                        }
                    }

                    // TODO apply reduction for others
                    if (age != null && age < 15) {
                        return "{ \"cost\": " + (int) Math.ceil(basePrice * .7) + "}";
                    } else {
                        if (age == null) {
                            double cost = basePrice * (1 - priceReductionPercent / 100.0);
                            return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                        } else {
                            if (age > 64) {
                                double cost = basePrice * .75 * (1 - priceReductionPercent / 100.0);
                                return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                            } else {
                                double cost = basePrice * (1 - priceReductionPercent / 100.0);
                                return "{ \"cost\": " + (int) Math.ceil(cost) + "}";
                            }
                        }
                    }
                } else {
                    if (age != null && age >= 6) {
                        if (age > 64) {
                            return "{ \"cost\": " + (int) Math.ceil(basePrice * .4) + "}";
                        } else {
                            return "{ \"cost\": " + basePrice + "}";
                        }
                    } else {
                        return "{ \"cost\": 0}";
                    }
                }
            }
        });

        after((req, res) -> {
            res.type("application/json");
        });

        return connection;
    }

}


    public RateCalculator() {
        rates = new Rates();
    }

    public float getBaseRate() {
        String baseRateJson = rates.getBaseRateJSON();
        float baseRate = parseBaseRateFromJson(baseRateJson);
        return baseRate;
    }

    private float parseBaseRateFromJson(String baseRateJson) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(baseRateJson).getAsJsonObject();
        float cost = jsonObject.get("cost").getAsFloat();
        return cost;
    }
}
