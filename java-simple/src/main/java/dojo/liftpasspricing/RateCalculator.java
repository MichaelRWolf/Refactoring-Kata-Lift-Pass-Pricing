package dojo.liftpasspricing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class RateCalculator {
    private Prices prices;

    public RateCalculator() {
        prices = new Prices();
    }

    public float getBaseRate() {
        String baseRateJson = prices.getBaseRateJSON();
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
